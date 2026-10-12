package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.*;
import io.github.pilzi.database.enums.PollType;
import io.github.pilzi.database.utils.AgentVoteUtil;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.*;
import io.github.pilzi.discord.enums.Role;
import io.github.pilzi.discord.utils.GuildUtil;
import io.github.pilzi.discord.utils.Message.DropdownUtil;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class PollServiceImpl implements PollService {
    @NonNull
    public static final String AGENT_SELECTION_BUTTON_ID = "agent-selection-button";
    @NonNull
    public static final String DISCORD_EMOJI_PREFIX = "<";
    @NonNull
    public static final String LINE_BREAK_AT_THE_END_CODE = "\u200B";
    @NonNull
    public static final String EMBED_RED = "#FF4655";
    @NonNull
    public static final String USER_NOT_FOUND_FALLBACK = "Unknown user";
    public static final String AGENT_VOTE_NOT_POSSIBLE_ANYMORE = "Since the week is over, voting is no longer possible";
    public static final String CLICK_HERE_TO_GO_TO_THE_CURRENT_POLL_TEXT = "Click here to go to the current poll ";

    @NonNull
    private final EventWorker eventWorker;

    @NonNull
    private final GuildWorker guildWorker;

    @NonNull
    private final ActivePollWorker activePollWorker;

    @NonNull
    private final ActivePollEventReferenceWorker activePollEventReferenceWorker;

    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;

    @NonNull
    private final ActivePollVoteWorker activePollVoteWorker;

    @NonNull
    private final AgentVoteWorker agentVoteWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker, @NonNull GuildWorker guildWorker, @NonNull ActivePollWorker activePollWorker, @NonNull ActivePollEventReferenceWorker activePollEventReferenceWorker, @NonNull HibernateSessionFactoryService hibernateSessionFactoryService, @NonNull ActivePollVoteWorker activePollVoteWorker, @NonNull AgentVoteWorker agentVoteWorker) {
        this.eventWorker = eventWorker;
        this.guildWorker = guildWorker;
        this.activePollWorker = activePollWorker;
        this.activePollEventReferenceWorker = activePollEventReferenceWorker;
        this.hibernateSessionFactoryService = hibernateSessionFactoryService;
        this.activePollVoteWorker = activePollVoteWorker;
        this.agentVoteWorker = agentVoteWorker;
    }

    @Override
    public void handlePollsForGuilds(@NonNull List<Guild> guilds) {
        handleScheduledPollForAllGuilds(guilds);
    }

    private void handleScheduledPollForAllGuilds(@NonNull List<Guild> guilds) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            guilds.forEach(guild -> {
                GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);
                ActivePollEntity activePollEntity = activePollWorker.getScheduledActivePoll(session, guild.getIdLong());

                if (isActivePollExpired(activePollEntity)) {
                    session.remove(activePollEntity);
                    activePollEntity = null;
                    session.flush();
                }

                if (doesActivePollNotExist(activePollEntity)) {
                    ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(), CalendarUtil.getLastInstantOfCurrentWeek(), PollType.SCHEDULE, guildEntity);
                    session.persist(newCreatedActivePollEntity);

                    eventsInCurrentWeekToPollEventReferences(eventsInCurrentWeek, newCreatedActivePollEntity, session);

                    Optional<TextChannel> textChannelOptional = GuildUtil.findChannelInGuild(guild);

                    if (textChannelOptional.isPresent()) {
                        sendScheduledPoll(textChannelOptional.get(), eventsInCurrentWeek, newCreatedActivePollEntity);
                    }
                }
            });
        });
    }

    /**
     * Gives each {@link ActivePollEventReferenceEntity} an increasing index
     * based on time, so the earliest event comes first. This allows us to
     * tell the poll choices apart when saving them via Hibernate.
     *
     * @param eventsInCurrentWeek        All event entities for the current week
     * @param newCreatedActivePollEntity The poll object currently being created
     * @param session                    The active Hibernate session
     */
    private static void eventsInCurrentWeekToPollEventReferences(@NonNull List<EventEntity> eventsInCurrentWeek, @NonNull ActivePollEntity newCreatedActivePollEntity, @NonNull Session session) {
        AtomicInteger orderIndex = new AtomicInteger(1);

        eventsInCurrentWeek.stream().sorted(Comparator.comparing(EventEntity::getStartAt)).map((eventInCurrentWeek) -> {
            int orderIndexValue = orderIndex.get();
            orderIndex.set(orderIndexValue + 1);
            return new ActivePollEventReferenceEntity(newCreatedActivePollEntity, eventInCurrentWeek, orderIndexValue);
        }).forEach(session::persist);
    }

    private static boolean doesActivePollNotExist(@Nullable ActivePollEntity activePollEntity) {
        return isActivePollOrMessageIdNull(activePollEntity);
    }

    private static boolean isActivePollExpired(@Nullable ActivePollEntity activePollEntity) {
        return activePollEntity != null && !CalendarUtil.doesMatchWeekBounds(activePollEntity.getStartAt(), activePollEntity.getEndAt());
    }


    private static void sendScheduledPoll(@NonNull GuildChannel guildChannel, @NonNull List<EventEntity> eventsInCurrentWeek, @NonNull ActivePollEntity newCreatedActivePollEntity) {
        String channelId = guildChannel.getId();
        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
        if (textChannel != null) {
            Message sentMessage = textChannel.sendMessage("").setPoll(PollUtil.buildEventPoll(getMap(eventsInCurrentWeek), eventsInCurrentWeek)).addComponents(ActionRow.of(Button.primary(AGENT_SELECTION_BUTTON_ID, "Select Agents"))).complete();

            newCreatedActivePollEntity.setMessageId(sentMessage.getIdLong());
        }
    }

    @Override
    public void replyToSelectAgentsButtonEventWithAgentPoll(@NonNull ButtonInteractionEvent event) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity scheduledActivePoll = activePollWorker.getScheduledActivePoll(session, Objects.requireNonNull(event.getGuild()).getIdLong());

            if (isActivePollOrMessageIdNull(scheduledActivePoll)) {
                replyToEventWithVoteNotPossible(event, AGENT_VOTE_NOT_POSSIBLE_ANYMORE);
            } else if (scheduledActivePoll.getMessageId() != event.getMessageIdLong()) {
                event.getChannel().retrieveMessageById(scheduledActivePoll.getMessageId()).queue(message -> {
                    String jumpUrl = message.getJumpUrl();
                    replyToEventWithVoteNotPossible(event, AGENT_VOTE_NOT_POSSIBLE_ANYMORE + "\n" + CLICK_HERE_TO_GO_TO_THE_CURRENT_POLL_TEXT + jumpUrl);
                }, _ -> replyToEventWithVoteNotPossible(event, AGENT_VOTE_NOT_POSSIBLE_ANYMORE));

            } else {
                replyToEventWithAgentPoll(event, session, scheduledActivePoll);
            }
        });
    }

    private void replyToEventWithAgentPoll(@NonNull ButtonInteractionEvent event, Session session, ActivePollEntity scheduledActivePoll) {
        List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);
        List<AgentVoteEntity> agentVotes = agentVoteWorker.getVotesForUser(session, Objects.requireNonNull(scheduledActivePoll.getId()), event.getUser().getIdLong());

        String text = "## ⚔️ Agent Selection\n" + "Which agents would you like to play on " + "**" + getMap(eventsInCurrentWeek) + "**?\n" + LINE_BREAK_AT_THE_END_CODE;

        Map<Role, List<String>> userForAgents = AgentVoteUtil.collectAgentsForRole(agentVotes);

        List<ActionRow> actionRows = new ArrayList<>(DropdownUtil.buildAgendActionRows(userForAgents));

        event.reply(text).addComponents(actionRows)
                .setEphemeral(true)
                .queue();
    }

    private static void replyToEventWithVoteNotPossible(@NonNull ButtonInteractionEvent event, String agentVoteNotPossibleAnymore) {
        event.reply(agentVoteNotPossibleAnymore)
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
    public void addScheduleVote(long userId, long answerId, long messageId) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEventReferenceEntity activePollEventReferenceEntity = activePollEventReferenceWorker.findByMessageIdAndIndex(session, messageId, answerId);

            if (activePollEventReferenceEntity != null) {
                session.persist(new ActivePollVoteEntity(userId, activePollEventReferenceEntity));
            }
        });
    }

    @Override
    public void removeScheduleVote(long userId, long answerId, long messageId) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollVoteEntity activePollVoteEntity = activePollVoteWorker.find(session, answerId, messageId, userId);

            if (activePollVoteEntity != null) {
                session.remove(activePollVoteEntity);
            }
        });
    }

    @Override
    public void importSelectedAgents(@NonNull Long guildId, @NonNull Long messageId, @NonNull Long discordUserId, @NonNull List<String> selectedAgents, @NonNull Role role) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            // Delete all existing agents with this specific role
            agentVoteWorker.deleteVoteByRole(session, role, discordUserId);
            ActivePollEntity scheduledActivePoll = Objects.requireNonNull(activePollWorker.getScheduledActivePoll(session, guildId));

            selectedAgents.forEach(agent -> session.persist(new AgentVoteEntity(discordUserId, agent, role, scheduledActivePoll)));
        });
    }

    @Override
    public void updateAgentPollForSingleGuild(@NonNull Guild guild) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            ActivePollEntity scheduledActivePoll = activePollWorker.getScheduledActivePoll(session, guild.getIdLong());
            Optional<TextChannel> textChannelOptional = GuildUtil.findChannelInGuild(guild);

            if (textChannelOptional.isPresent()) {
                List<AgentVoteEntity> agentVotes = agentVoteWorker.getAllVotes(session, Objects.requireNonNull(scheduledActivePoll.getId()));
                Map<Long, List<String>> userForAgents = AgentVoteUtil.collectAgentsForUser(agentVotes);

                List<Long> userIdsToFetch = new ArrayList<>(userForAgents.keySet());

                textChannelOptional.get().retrieveMessageById(scheduledActivePoll.getMessageId()).queue(message -> {
                    if (!userForAgents.isEmpty()) {
                        message.suppressEmbeds(false).queue();

                        guild.retrieveMembersByIds(userIdsToFetch).onSuccess(members -> {
                            Map<Long, String> memberNames = getMemberIdForMemberMap(members);

                            EmbedBuilder embedBuilder = createUserAgentsDisplayEmbedBuilder(userForAgents, memberNames);
                            message.editMessageEmbeds(embedBuilder.build()).complete();
                        });
                    } else {
                        message.suppressEmbeds(true).queue();
                    }
                });
            }
        });
    }

    private static @NonNull EmbedBuilder createUserAgentsDisplayEmbedBuilder(@NonNull Map<Long /* discord user id */, List<String /* agent name */>> userForAgents,
                                                                             @NonNull Map<Long, String> memberNames) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        StringBuilder description = new StringBuilder();

        userForAgents.forEach((discordUserId, agents) -> {
            String name = memberNames.getOrDefault(discordUserId, USER_NOT_FOUND_FALLBACK);
            List<String> agentsWithName = agents.stream().filter(agent -> !agent.startsWith(DISCORD_EMOJI_PREFIX)).toList();
            List<String> agentsWithEmoji = agents.stream().filter(agent -> agent.startsWith(DISCORD_EMOJI_PREFIX)).toList();

            description.append("## ").append(name);

            if (!agentsWithEmoji.isEmpty()) {
                description.append("\n").append("## ").append(String.join(" ", agentsWithEmoji));
            }
            if (!agentsWithName.isEmpty()) {
                description.append("\n").append(String.join(", ", agentsWithName));
            }

            description.append("\n");
        });

        embedBuilder.setDescription(description.toString());
        embedBuilder.setColor(Color.decode(EMBED_RED));
        return embedBuilder;
    }

    private static @NonNull Map<Long, String> getMemberIdForMemberMap(@NonNull List<Member> members) {
        Map<Long, String> memberNames = new HashMap<>();
        for (Member m : members) {
            memberNames.put(m.getIdLong(), m.getEffectiveName());
        }
        return memberNames;
    }
}