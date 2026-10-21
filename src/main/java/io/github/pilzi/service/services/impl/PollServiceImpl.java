package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.domain.AgentVoteEntity;
import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.GuildEntity;
import io.github.pilzi.database.utils.AgentVoteUtil;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.ActivePollWorker;
import io.github.pilzi.database.workers.AgentVoteWorker;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.GuildWorker;
import io.github.pilzi.discord.enums.Role;
import io.github.pilzi.discord.utils.GuildUtil;
import io.github.pilzi.discord.utils.LanguageUtil;
import io.github.pilzi.discord.utils.Message.ButtonUtil;
import io.github.pilzi.discord.utils.Message.DropdownUtil;
import io.github.pilzi.discord.utils.Message.EmbedUtil;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


public class PollServiceImpl implements PollService {
    @NonNull
    private static final Logger logger = LoggerFactory.getLogger(PollServiceImpl.class);
    @NonNull
    public static final String LINE_BREAK_AT_THE_END_CODE = "\u200B";

    @NonNull
    private final EventWorker eventWorker;

    @NonNull
    private final GuildWorker guildWorker;

    @NonNull
    private final ActivePollWorker activePollWorker;

    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;

    @NonNull
    private final AgentVoteWorker agentVoteWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker,
                           @NonNull GuildWorker guildWorker,
                           @NonNull ActivePollWorker activePollWorker,
                           @NonNull HibernateSessionFactoryService hibernateSessionFactoryService,
                           @NonNull AgentVoteWorker agentVoteWorker) {
        this.eventWorker = eventWorker;
        this.guildWorker = guildWorker;
        this.activePollWorker = activePollWorker;
        this.hibernateSessionFactoryService = hibernateSessionFactoryService;
        this.agentVoteWorker = agentVoteWorker;
    }

    @Override
    public @NonNull List<? extends CompletableFuture<?>> handleScheduledPollForAllGuilds(@NonNull List<Guild> guilds) {
        List<CompletableFuture<?>> scheduledPolls = new ArrayList<>();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            if (!eventsInCurrentWeek.isEmpty()) {
                guilds.forEach(guild -> {
                    try {
                        handleScheduledPollForGuild(session, guild, eventsInCurrentWeek, scheduledPolls);
                    } catch (Exception e) {
                        logger.error("unable to send active poll to guild with name: {}", guild.getName(), e);
                    }
                });
            }
        });

        return scheduledPolls;
    }

    private void handleScheduledPollForGuild(Session session, Guild guild, List<EventEntity> eventsInCurrentWeek, List<CompletableFuture<?>> scheduledPolls) {
        GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);
        ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, guild.getIdLong());

        if (isActivePollExpired(activePollEntity)) {
            session.remove(activePollEntity);
            activePollEntity = null;
            session.flush();
        }

        if (isActivePollOrMessageIdNull(activePollEntity)) {
            ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                    CalendarUtil.getLastInstantOfCurrentWeek(),
                    guildEntity);
            session.persist(newCreatedActivePollEntity);

            Optional<TextChannel> textChannelOptional = GuildUtil.findChannelInGuild(guild);

            if (textChannelOptional.isPresent()) {
                CompletableFuture<Void> future = sendScheduledPoll(textChannelOptional.get(), eventsInCurrentWeek, guild.getIdLong());
                if (future != null) {
                    scheduledPolls.add(future);
                }
            }
        }
    }

    private static boolean isActivePollExpired(@Nullable ActivePollEntity activePollEntity) {
        return activePollEntity != null && !CalendarUtil.doesMatchWeekBounds(activePollEntity.getStartAt(), activePollEntity.getEndAt());
    }

    @Nullable
    private CompletableFuture<Void> sendScheduledPoll(@NonNull GuildChannel guildChannel,
                                                      @NonNull List<EventEntity> eventsInCurrentWeek,
                                                      @NonNull Long guildId) {
        String channelId = guildChannel.getId();
        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
        if (textChannel != null) {
            new CompletableFuture<>();
            return textChannel.sendMessage("")
                    .setPoll(PollUtil.buildEventPoll(getMap(eventsInCurrentWeek), eventsInCurrentWeek))
                    .addComponents(ButtonUtil.getAgentSelectionButton())
                    .submit()
                    .thenAcceptAsync(msg -> TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
                        ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, guildId);
                        activePollEntity.setMessageId(msg.getIdLong());
                    }));
        }

        return null;
    }

    @Override
    public void replyToSelectAgentsButtonEventWithAgentPoll(@NonNull ButtonInteractionEvent event) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity scheduledActivePoll = activePollWorker.getActivePoll(session, Objects.requireNonNull(event.getGuild()).getIdLong());

            if (isActivePollOrMessageIdNull(scheduledActivePoll)) {
                replyToEventWithVoteNotPossible(event, LanguageUtil.getAgentVoteNotPossibleAnymore());
            } else if (scheduledActivePoll.getMessageId() != event.getMessageIdLong()) {
                replyToEventWithVoteNotPossibleAndLinkToActivePoll(event, scheduledActivePoll);
            } else {
                replyToEventWithAgentPoll(event, session, scheduledActivePoll);
            }
        });
    }

    private static void replyToEventWithVoteNotPossibleAndLinkToActivePoll(@NonNull ButtonInteractionEvent event,
                                                                           @NonNull ActivePollEntity scheduledActivePoll) {
        event.getChannel().retrieveMessageById(scheduledActivePoll.getMessageId()).queue(message -> {
            String jumpUrl = message.getJumpUrl();
            replyToEventWithVoteNotPossible(event, getVoteNotPossibleWithJumpUrl(jumpUrl));
        }, _ -> replyToEventWithVoteNotPossible(event, LanguageUtil.getAgentVoteNotPossibleAnymore()));
    }

    private static @NonNull String getVoteNotPossibleWithJumpUrl(@NonNull String jumpUrl) {
        return LanguageUtil.getAgentVoteNotPossibleAnymore() + "\n" + LanguageUtil.getClickHereToGoToPollText() + " " + jumpUrl;
    }

    private void replyToEventWithAgentPoll(@NonNull ButtonInteractionEvent event,
                                           @NonNull Session session,
                                           @NonNull ActivePollEntity scheduledActivePoll) {
        List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

        if (eventsInCurrentWeek.isEmpty()) {
            replyToEventWithVoteNotPossible(event, LanguageUtil.getAgentVoteNotPossibleAnymore());
        } else {
            List<AgentVoteEntity> agentVotes = agentVoteWorker.getVotesForUser(session, Objects.requireNonNull(scheduledActivePoll.getId()), event.getUser().getIdLong());

            String text = LanguageUtil.getAgentSelectionText("**" + getMap(eventsInCurrentWeek) + "**") + "\n" + LINE_BREAK_AT_THE_END_CODE;

            Map<Role, List<String>> userForAgents = AgentVoteUtil.collectAgentsForRole(agentVotes);

            List<ActionRow> actionRows = new ArrayList<>(DropdownUtil.buildAgendActionRows(userForAgents));

            event.reply(text).addComponents(actionRows)
                    .setEphemeral(true)
                    .queue();
        }
    }

    private static void replyToEventWithVoteNotPossible(@NonNull ButtonInteractionEvent event,
                                                        @NonNull String voteNotPossibleMessage) {
        event.reply(voteNotPossibleMessage)
                .setEphemeral(true)
                .queue();
    }

    private static boolean isActivePollOrMessageIdNull(@Nullable ActivePollEntity scheduledActivePoll) {
        return scheduledActivePoll == null || scheduledActivePoll.getMessageId() == null;
    }

    private static @NonNull String getMap(@NonNull List<EventEntity> eventsInCurrentWeek) {
        return eventsInCurrentWeek.getFirst().getMap();
    }

    @Override
    public void importSelectedAgents(@NonNull Long guildId,
                                     @NonNull Long messageId,
                                     @NonNull Long discordUserId,
                                     @NonNull List<String> selectedAgents,
                                     @NonNull Role role) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            // Delete all existing agents with this specific role
            agentVoteWorker.deleteVoteByRole(session, role, discordUserId);
            ActivePollEntity scheduledActivePoll = Objects.requireNonNull(activePollWorker.getActivePoll(session, guildId));
            // Save all selected agents in database
            selectedAgents.forEach(agent -> session.persist(new AgentVoteEntity(discordUserId, agent, role, scheduledActivePoll)));
        });
    }

    @Nullable
    private CompletableFuture<?> updateAgentPollForSingleGuild(@NonNull Guild guild) {

        List<AgentVoteEntity> agentVotes = new ArrayList<>();
        AtomicReference<ActivePollEntity> scheduledActivePoll = new AtomicReference<>();

        Optional<TextChannel> textChannelOptional = GuildUtil.findChannelInGuild(guild);

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            scheduledActivePoll.set(activePollWorker.getActivePoll(session, guild.getIdLong()));

            if (textChannelOptional.isPresent()) {
                agentVotes.addAll(agentVoteWorker.getAllVotes(session, Objects.requireNonNull(scheduledActivePoll.get().getId())));
            }
        });

        if (textChannelOptional.isPresent()) {
            Map<Long, List<String>> userForAgents = AgentVoteUtil.collectAgentsForUser(agentVotes);
            try {
                Message message = textChannelOptional.get().retrieveMessageById(scheduledActivePoll.get().getMessageId()).submit().get();

                if (!userForAgents.isEmpty()) {
                    return updateMessageAgentEmbeds(guild, message, userForAgents);
                } else {
                    return message.suppressEmbeds(true).submit();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public @NonNull List<? extends CompletableFuture<?>> updateAgentPollForGuilds(@NonNull Set<Guild> guilds) {
        return guilds.stream()
                .map(guild -> {
                    try {
                        return this.updateAgentPollForSingleGuild(guild);
                    } catch (Exception e) {
                        logger.error("update agent poll error: guildName={} guildId={}", guild.getName(), guild.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @NonNull
    private static CompletableFuture<Message> updateMessageAgentEmbeds(@NonNull Guild guild,
                                                                       @NonNull Message message,
                                                                       @NonNull Map<Long, List<String>> userForAgents) {
        message.suppressEmbeds(false).complete();
        List<Long> userIdsToFetch = new ArrayList<>(userForAgents.keySet());

        List<Member> members = guild.retrieveMembersByIds(userIdsToFetch).get();
        Map<Long, String> memberNames = getMemberIdForMemberMap(members);

        EmbedBuilder embedBuilder = EmbedUtil.createUserAgentsDisplayEmbedBuilder(userForAgents, memberNames);
        return message.editMessageEmbeds(embedBuilder.build()).submit();
    }

    private static @NonNull Map<Long, String> getMemberIdForMemberMap(@NonNull List<Member> members) {
        Map<Long, String> memberNames = new HashMap<>();
        for (Member m : members) {
            memberNames.put(m.getIdLong(), m.getEffectiveName());
        }
        return memberNames;
    }
}