package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.*;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.ActivePollEventReferenceWorker;
import io.github.pilzi.database.workers.ActivePollVoteWorker;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.GuildWorker;
import io.github.pilzi.discord.utils.GuildUtil;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PollServiceImpl implements PollService {

    @NonNull
    private final EventWorker eventWorker;

    @NonNull
    private final GuildWorker guildWorker;

    @NonNull
    private final ActivePollEventReferenceWorker activePollEventReferenceWorker;

    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;

    @NonNull
    private final ActivePollVoteWorker activePollVoteWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker,
                           @NonNull GuildWorker guildWorker,
                           @NonNull ActivePollEventReferenceWorker activePollEventReferenceWorker,
                           @NonNull HibernateSessionFactoryService hibernateSessionFactoryService,
                           @NonNull ActivePollVoteWorker activePollVoteWorker) {
        this.eventWorker = eventWorker;
        this.guildWorker = guildWorker;
        this.activePollEventReferenceWorker = activePollEventReferenceWorker;
        this.hibernateSessionFactoryService = hibernateSessionFactoryService;
        this.activePollVoteWorker = activePollVoteWorker;
    }

    @Override
    public void handlePollForAllGuilds(@NonNull List<Guild> guilds) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            guilds.forEach(guild -> {
                GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);

                ActivePollEntity activePollEntity = guildEntity.getActivePoll();

                if (isActivePollExpiredOrNull(activePollEntity)) {
                    activePollEntity = null;
                    guildEntity.setActivePoll(null);
                }

                if (doesActivePollNotExist(activePollEntity)) {
                    ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                            CalendarUtil.getLastInstantOfCurrentWeek(),
                            guildEntity);
                    session.persist(newCreatedActivePollEntity);
                    guildEntity.setActivePoll(newCreatedActivePollEntity);

                    eventsInCurrentWeekToPollEventReferences(eventsInCurrentWeek, newCreatedActivePollEntity, session);

                    Optional<GuildChannel> guildChannelOptional = GuildUtil.findChannelInGuild(guild);

                    if (guildChannelOptional.isPresent()) {
                        sendPoll(guildChannelOptional.get(), eventsInCurrentWeek, newCreatedActivePollEntity);
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

    private static boolean doesActivePollNotExist(ActivePollEntity activePollEntity) {
        return activePollEntity == null || activePollEntity.getMessageId() == null;
    }

    private static boolean isActivePollExpiredOrNull(ActivePollEntity activePollEntity) {
        return activePollEntity != null && !CalendarUtil.doesMatchWeekBounds(activePollEntity.getStartAt(), activePollEntity.getEndAt());
    }


    private static void sendPoll(@NonNull GuildChannel guildChannel,
                                 List<EventEntity> eventsInCurrentWeek,
                                 ActivePollEntity newCreatedActivePollEntity) {
        String channelId = guildChannel.getId();
        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
        if (textChannel != null) {
            Message sentMessage = textChannel.sendMessage("")
                    .setPoll(PollUtil.buildEventPoll(eventsInCurrentWeek.getFirst().getMap(), eventsInCurrentWeek))
                    .complete();

            newCreatedActivePollEntity.setMessageId(sentMessage.getIdLong());
        }
    }

    @Override
    public void addVote(long userId,
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
    public void removeVote(long userId,
                           long answerId,
                           long messageId) {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollVoteEntity activePollVoteEntity = activePollVoteWorker.find(session, answerId, messageId, userId);

            if (activePollVoteEntity != null) {
                session.remove(activePollVoteEntity);
            }
        });
    }
}