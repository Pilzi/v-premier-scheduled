package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.*;
import io.github.pilzi.database.enums.PollType;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.*;
import io.github.pilzi.discord.enums.Agent;
import io.github.pilzi.discord.enums.Role;
import io.github.pilzi.discord.utils.GuildUtil;
import io.github.pilzi.discord.utils.Message.DropdownUtil;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.pilzi.discord.listener.BotListener.AGENT_SUBMIT;

public class PollServiceImpl implements PollService {

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

    public PollServiceImpl(@NonNull EventWorker eventWorker,
                           @NonNull GuildWorker guildWorker,
                           @NonNull ActivePollWorker activePollWorker,
                           @NonNull ActivePollEventReferenceWorker activePollEventReferenceWorker,
                           @NonNull HibernateSessionFactoryService hibernateSessionFactoryService,
                           @NonNull ActivePollVoteWorker activePollVoteWorker,
                           @NonNull AgentVoteWorker agentVoteWorker) {
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
        handleAgentPollForAllGuilds(guilds);
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
                }

                if (doesActivePollNotExist(activePollEntity)) {
                    ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                            CalendarUtil.getLastInstantOfCurrentWeek(),
                            PollType.SCHEDULE,
                            guildEntity);
                    session.persist(newCreatedActivePollEntity);

                    eventsInCurrentWeekToPollEventReferences(eventsInCurrentWeek, newCreatedActivePollEntity, session);

                    Optional<GuildChannel> guildChannelOptional = GuildUtil.findChannelInGuild(guild);

                    if (guildChannelOptional.isPresent()) {
                        sendScheduledPoll(guildChannelOptional.get(), eventsInCurrentWeek, newCreatedActivePollEntity);
                    }
                }
            });
        });
    }

    private void handleAgentPollForAllGuilds(@NonNull List<Guild> guilds) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            guilds.forEach(guild -> {
                GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);
                ActivePollEntity activeAgentPollEntity = activePollWorker.getAgentActivePoll(session, guild.getIdLong());

                if (isActivePollExpired(activeAgentPollEntity)) {
                    session.remove(activeAgentPollEntity);
                    activeAgentPollEntity = null;
                }

                if (doesActivePollNotExist(activeAgentPollEntity)) {
                    ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                            CalendarUtil.getLastInstantOfCurrentWeek(),
                            PollType.AGENT,
                            guildEntity);
                    session.persist(newCreatedActivePollEntity);


                    Optional<GuildChannel> guildChannelOptional = GuildUtil.findChannelInGuild(guild);

                    if (guildChannelOptional.isPresent()) {
                        sendAgentPoll(guildChannelOptional.get(), eventsInCurrentWeek, newCreatedActivePollEntity);
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
    private static void eventsInCurrentWeekToPollEventReferences(@NonNull List<EventEntity> eventsInCurrentWeek,
                                                                 @NonNull ActivePollEntity newCreatedActivePollEntity,
                                                                 @NonNull Session session) {
        AtomicInteger orderIndex = new AtomicInteger(1);

        eventsInCurrentWeek.stream()
                .sorted(Comparator.comparing(EventEntity::getStartAt))
                .map((eventInCurrentWeek) -> {
                    int orderIndexValue = orderIndex.get();
                    orderIndex.set(orderIndexValue + 1);
                    return new ActivePollEventReferenceEntity(newCreatedActivePollEntity, eventInCurrentWeek, orderIndexValue);
                })
                .forEach(session::persist);
    }

    private static boolean doesActivePollNotExist(@Nullable ActivePollEntity activePollEntity) {
        return activePollEntity == null || activePollEntity.getMessageId() == null;
    }

    private static boolean isActivePollExpired(@Nullable ActivePollEntity activePollEntity) {
        return activePollEntity != null && !CalendarUtil.doesMatchWeekBounds(activePollEntity.getStartAt(), activePollEntity.getEndAt());
    }


    private static void sendScheduledPoll(@NonNull GuildChannel guildChannel,
                                          @NonNull List<EventEntity> eventsInCurrentWeek,
                                          @NonNull ActivePollEntity newCreatedActivePollEntity) {
        String channelId = guildChannel.getId();
        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
        if (textChannel != null) {
            Message sentMessage = textChannel.sendMessage("")
                    .setPoll(PollUtil.buildEventPoll(getMap(eventsInCurrentWeek), eventsInCurrentWeek))
                    .complete();

            newCreatedActivePollEntity.setMessageId(sentMessage.getIdLong());
        }
    }

    private static void sendAgentPoll(@NonNull GuildChannel guildChannel,
                                      @NonNull List<EventEntity> eventsInCurrentWeek,
                                      @NonNull ActivePollEntity newCreatedActivePollEntity) {
        String channelId = guildChannel.getId();
        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
        if (textChannel != null) {
            String text = "## ⚔\uFE0F Agent Selection\n" +
                    "Which agents would you like to play on " +
                    "**" + getMap(eventsInCurrentWeek) + "**?\n" +
                    "\u200B";
            List<ActionRow> actionRows = new ArrayList<>(DropdownUtil.buildAgendActionRows());
            addAgentPollSubmitButton(actionRows);

            Message sentMessage = textChannel.sendMessage(text)
                    .addComponents(actionRows)
                    .complete();

            newCreatedActivePollEntity.setMessageId(sentMessage.getIdLong());
        }
    }

    private static void addAgentPollSubmitButton(@NonNull List<ActionRow> actionRows) {
        Button submitButton = Button.success(AGENT_SUBMIT, "submit");
        ActionRow actionRow = ActionRow.of(submitButton);
        actionRows.add(actionRow);
    }

    private static @NonNull String getMap(@NonNull List<EventEntity> eventsInCurrentWeek) {
        return eventsInCurrentWeek.getFirst().getMap();
    }

    @Override
    public void addScheduleVote(long userId,
                                long answerId,
                                long messageId) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEventReferenceEntity activePollEventReferenceEntity = activePollEventReferenceWorker.findByMessageIdAndIndex(session, messageId, answerId);

            if (activePollEventReferenceEntity != null) {
                session.persist(new ActivePollVoteEntity(userId, activePollEventReferenceEntity));
            }
        });
    }

    @Override
    public void removeScheduleVote(long userId,
                                   long answerId,
                                   long messageId) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollVoteEntity activePollVoteEntity = activePollVoteWorker.find(session, answerId, messageId, userId);

            if (activePollVoteEntity != null) {
                session.remove(activePollVoteEntity);
            }
        });
    }

    @Override
    public void importSelectedAgents(@NonNull Long guildId,
                                     @NonNull Long messageId,
                                     @NonNull Long discordUserId,
                                     @NonNull List<String> selectedAgents) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            // The poll is grouped by roles each agent have the same role in this list
            Role agentRole = Agent.getRoleByName(selectedAgents.getFirst());
            // Delete all existing agents with this specific role
            agentVoteWorker.deleteVoteByRole(session, agentRole, discordUserId);
            ActivePollEntity agentActivePoll = Objects.requireNonNull(activePollWorker.getAgentActivePoll(session, guildId));

            selectedAgents.forEach(agent -> session.persist(new AgentVoteEntity(discordUserId, agent, agentRole, agentActivePoll)));
        });
    }
}