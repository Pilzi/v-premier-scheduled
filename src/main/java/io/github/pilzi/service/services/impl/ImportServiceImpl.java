package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import io.github.pilzi.henrikdev.service.impl.RestServiceImpl;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.utils.PremierImportHelperUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ImportServiceImpl implements ImportService {
    @NonNull
    public static final String HIBERNATE_CFG_XML = "hibernate.cfg.xml";

    @Override
    public void importPremierData() {
        RestService restService = new RestServiceImpl("<Token>", "eu");

        List<Season> seasons = restService.getSeasons().data();

        Session session;
        try (SessionFactory sessionFactory = new Configuration().configure(HIBERNATE_CFG_XML).buildSessionFactory()) {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            List<SeasonEntity> seasonEntities = PremierImportHelperUtil.toSeasonEntity(seasons);

            persistAll(session, seasonEntities);
            List<EventEntity> eventEntities = PremierImportHelperUtil.toEventEntities(seasons, seasonEntities);
            persistAll(session, eventEntities);

            transaction.commit();
            session.flush();
            session.close();
        }

    }

    private static <T> void persistAll(@NonNull Session session,
                                       @NonNull List<T> entities) {
        for (T entity : entities) {
            session.persist(entity);
        }
    }
}
