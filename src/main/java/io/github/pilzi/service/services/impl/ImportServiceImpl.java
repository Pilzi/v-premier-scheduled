package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.database.utils.HibernateSessionFactoryUtil;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import io.github.pilzi.henrikdev.service.impl.RestServiceImpl;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.utils.PremierImportHelperUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.pilzi.discord.Bot.DOTENV;

public class ImportServiceImpl implements ImportService {

    @NonNull
    public static final String HENRIKDEV_API_KEY_ENV = "HENRIKDEV_API_KEY";
    @NonNull
    public static final String VALORANT_REGION_ENV = "VALORANT_REGION";

    @NonNull
    private final SeasonWorker seasonWorker;

    @NonNull
    private final EventWorker eventWorker;

    public ImportServiceImpl(@NonNull SeasonWorker seasonWorker,
                             @NonNull EventWorker eventWorker) {
        this.seasonWorker = seasonWorker;
        this.eventWorker = eventWorker;
    }

    @Override
    public void importPremierData() {
        RestService restService = new RestServiceImpl(DOTENV.get(HENRIKDEV_API_KEY_ENV), DOTENV.get(VALORANT_REGION_ENV));

        List<Season> seasons = restService.getSeasons().data();
        SessionFactory sessionFactory = HibernateSessionFactoryUtil.get();
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            List<SeasonEntity> existingSeasonEntities = seasonWorker.getAll(session);
            List<SeasonEntity> seasonEntities = PremierImportHelperUtil.toSeasonEntity(seasons);


            List<SeasonEntity> seasonsToPersist = new ArrayList<>();
            List<SeasonEntity> managedSeasonEntities = new ArrayList<>();
            seasonEntities.forEach(seasonEntity -> {
                Optional<SeasonEntity> matchingExistingEntity = existingSeasonEntities.stream()
                        .filter(existingEntity -> existingEntity.getExternalId().equals(seasonEntity.getExternalId()))
                        .findFirst();

                if (matchingExistingEntity.isPresent()) {
                    SeasonEntity existingSeason = matchingExistingEntity.get();
                    seasonWorker.update(existingSeason, seasonEntity);
                    managedSeasonEntities.add(existingSeason);
                } else {
                    seasonsToPersist.add(seasonEntity);
                    managedSeasonEntities.add(seasonEntity);
                }
            });

            persistAll(session, seasonsToPersist);

            List<EventEntity> existingEventEntities = new ArrayList<>(eventWorker.getAll(session));
            List<EventEntity> eventEntities = PremierImportHelperUtil.toEventEntities(seasons, seasonEntities);

            List<EventEntity> eventsToPersist = new ArrayList<>();
            eventEntities.forEach(eventEntity -> {
                Optional<EventEntity> matchingExistingEntity = existingEventEntities.stream()
                        .filter(existingEntity -> existingEntity.equals(eventEntity))
                        .findFirst();

                if (matchingExistingEntity.isPresent()) {
                    EventEntity existingEntity = matchingExistingEntity.get();
                    existingEventEntities.remove(existingEntity);

                    SeasonEntity managedSeason = managedSeasonEntities.stream()
                            .filter(season -> season.getExternalId().equals(existingEntity.getSeason().getExternalId()))
                            .findFirst()
                            .orElseThrow();

                    eventWorker.update(existingEntity,
                            eventEntity,
                            managedSeason);
                } else {
                    SeasonEntity parentSeason = managedSeasonEntities.stream()
                            .filter(seasonEntity -> seasonEntity.getExternalId().equals(eventEntity.getSeason().getExternalId()))
                            .findFirst()
                            .orElseThrow();

                    eventEntity.setSeason(parentSeason);

                    eventsToPersist.add(eventEntity);
                }
            });

            existingEventEntities.forEach(unusedEntity -> {
                unusedEntity.setSeason(null);
                session.remove(unusedEntity);
            });

            persistAll(session, eventsToPersist);

            transaction.commit();
            session.flush();
        }
    }

    private static <T> void persistAll(@NonNull Session session,
                                       @NonNull List<T> entities) {
        for (T entity : entities) {
            session.persist(entity);
        }
    }
}
