package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.GuildEntity;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.GuildWorker;
import io.github.pilzi.discord.utils.Message.PollUtil;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.utils.CalendarUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

import static io.github.pilzi.discord.listener.BotListener.CHANNEL_NAME;
import static io.github.pilzi.service.services.impl.ImportServiceImpl.HIBERNATE_CFG_XML;

public class PollServiceImpl implements PollService {

    @NonNull
    private final EventWorker eventWorker;

    @NonNull
    private final GuildWorker guildWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker,
                           @NonNull GuildWorker guildWorker) {
        this.eventWorker = eventWorker;
        this.guildWorker = guildWorker;
    }

    @Override
    public void handlePollForAllGuilds(@NonNull List<Guild> guilds) {
        Session session;
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML)
                .buildSessionFactory()) {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            guilds.forEach(guild -> {
                GuildEntity guildEntity = guildWorker.getOrCreate(session, guild);

                ActivePollEntity activePollEntity = guildEntity.getActivePoll();

                if (activePollEntity != null && !CalendarUtil.isInCurrentWeek(activePollEntity.getStartAt(), activePollEntity.getEndAt())) {
                    activePollEntity = null;
                    guildEntity.setActivePoll(null);
                }

                if (activePollEntity == null) {
                    guildEntity.setActivePoll(new ActivePollEntity(CalendarUtil.getFirstInstantOfCurrentWeek(),
                            CalendarUtil.getLastInstantOfCurrentWeek(),
                            guildEntity,
                            eventsInCurrentWeek));

                    Optional<GuildChannel> guildChannelOptional = guild.getChannels().stream()
                            .filter(channel -> channel.getName().equals(CHANNEL_NAME))
                            .findFirst();

                    if (guildChannelOptional.isPresent()) {
                        GuildChannel guildChannel = guildChannelOptional.get();

                        String channelId = guildChannel.getId();
                        TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
                        if (textChannel != null) {

                            textChannel.sendMessage("")
                                    .setPoll(PollUtil.buildEventPoll(eventsInCurrentWeek.getFirst().getMap(), eventsInCurrentWeek))
                                    .queue(msg -> System.out.println("message id: " + msg.getIdLong()));
                        }
                    }
                }
            });

            transaction.commit();
            session.flush();
            session.close();
        }
    }
}