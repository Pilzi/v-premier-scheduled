package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.service.services.PollService;
import net.dv8tion.jda.api.entities.Guild;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static io.github.pilzi.service.services.impl.ImportServiceImpl.HIBERNATE_CFG_XML;

public class PollServiceImpl implements PollService {

    @NonNull
    private final EventWorker eventWorker;

    public PollServiceImpl(@NonNull EventWorker eventWorker) {
        this.eventWorker = eventWorker;
    }

    @Override
    public void handlePollForAllGuilds(@NonNull List<Guild> guilds) {
        Session session;
        System.out.println("test");
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML).buildSessionFactory()) {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            List<EventEntity> eventsInCurrentWeek = eventWorker.collectEventsForCurrentWeek(session);

            transaction.commit();
            session.flush();
            session.close();
        }
    }
}