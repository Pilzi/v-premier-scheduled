package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.*;
import io.github.pilzi.database.workers.ActivePollEventReferenceWorker;
import io.github.pilzi.database.workers.ActivePollVoteWorker;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.GuildWorker;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.pilzi.discord.listener.BotListener.CHANNEL_NAME;
import static io.github.pilzi.service.services.impl.ImportServiceImpl.HIBERNATE_CFG_XML;

public class PollServiceImpl implements PollService {

    @NonNull
    private final EventWorker eventWorker;

    @NonNull
    private final GuildWorker guildWorker;

    @NonNull
    private final ActivePollEventReferenceWorker activePollEventReferenceWorker;

    @NonNull
    private final ActivePollVoteWorker activePollVoteWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker,
                           @NonNull GuildWorker guildWorker,
                           @NonNull ActivePollEventReferenceWorker activePollEventReferenceWorker,
                           @NonNull ActivePollVoteWorker activePollVoteWorker) {
        this.eventWorker = eventWorker;
        this.guildWorker = guildWorker;
        this.activePollEventReferenceWorker = activePollEventReferenceWorker;
        this.activePollVoteWorker = activePollVoteWorker;
    }

    @Override
    public void handlePollForAllGuilds(@NonNull List<Guild> guilds) {
        Transaction transaction = null;
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML)
                .buildSessionFactory()) {
            Session session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            guilds.forEach(guild -> {
                GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);

                ActivePollEntity activePollEntity = guildEntity.getActivePoll();

                if (activePollEntity != null && !CalendarUtil.isInCurrentWeek(activePollEntity.getStartAt(), activePollEntity.getEndAt())) {
                    activePollEntity = null;
                    guildEntity.setActivePoll(null);
                }

                if (activePollEntity == null || activePollEntity.getMessageId() == null) {
                    ActivePollEntity newCreatedActivePollEntity = new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                            CalendarUtil.getLastInstantOfCurrentWeek(),
                            guildEntity);
                    session.persist(newCreatedActivePollEntity);
                    guildEntity.setActivePoll(newCreatedActivePollEntity);
                    AtomicInteger orderIndex = new AtomicInteger(1);
                    eventsInCurrentWeek.stream()
                            .sorted(Comparator.comparing(EventEntity::getStartAt))
                            .map((eventInCurrentWeek) -> {
                                int orderIndexValue = orderIndex.get();
                                orderIndex.set(orderIndexValue + 1);
                                return new ActivePollEventReferenceEntity(newCreatedActivePollEntity, eventInCurrentWeek, orderIndexValue);
                            })
                            .forEach(session::persist);

                    Optional<GuildChannel> guildChannelOptional = guild.getChannels().stream()
                            .filter(channel -> channel.getName().equals(CHANNEL_NAME))
                            .findFirst();

                    if (guildChannelOptional.isPresent()) {
                        GuildChannel guildChannel = guildChannelOptional.get();

                        String channelId = guildChannel.getId();
                        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
                        if (textChannel != null) {
                            Message sentMessage = textChannel.sendMessage("")
                                    .setPoll(PollUtil.buildEventPoll(eventsInCurrentWeek.getFirst().getMap(), eventsInCurrentWeek))
                                    .complete();

                            newCreatedActivePollEntity.setMessageId(sentMessage.getIdLong());
                        }
                    }
                }
            });
            transaction.commit();
            session.flush();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void addVote(long userId,
                        long answerId,
                        long messageId) {
        Transaction transaction = null;
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML)
                .buildSessionFactory()) {
            Session session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            ActivePollEventReferenceEntity activePollEventReferenceEntity = activePollEventReferenceWorker.findByMessageIdAndIndex(session, messageId, answerId);

            if (activePollEventReferenceEntity != null) {
                session.persist(new ActivePollVoteEntity(userId, activePollEventReferenceEntity));
            }

            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void removeVote(long userId,
                        long answerId,
                        long messageId) {
        Transaction transaction = null;
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML)
                .buildSessionFactory()) {
            Session session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            ActivePollVoteEntity activePollVoteEntity = activePollVoteWorker.find(session, answerId, messageId, userId);

            if (activePollVoteEntity != null) {
                session.remove(activePollVoteEntity);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}