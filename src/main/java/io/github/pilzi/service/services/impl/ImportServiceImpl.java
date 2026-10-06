package io.github.pilzi.service.services.impl;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.utils.PremierImportHelperUtil;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportServiceImpl implements ImportService {

    @NonNull
    public static final String HENRIKDEV_API_KEY_ENV = "HENRIKDEV_API_KEY";
    @NonNull
    public static final String VALORANT_REGION_ENV = "VALORANT_REGION";

    @NonNull
    private final RestService restService;

    @NonNull
    private final SeasonWorker seasonWorker;

    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;

    @NonNull
    private final EventWorker eventWorker;

    public ImportServiceImpl(@NonNull RestService restService,
                             @NonNull SeasonWorker seasonWorker,
                             @NonNull HibernateSessionFactoryService hibernateSessionFactoryService,
                             @NonNull EventWorker eventWorker) {
        this.restService = restService;
        this.seasonWorker = seasonWorker;
        this.hibernateSessionFactoryService = hibernateSessionFactoryService;
        this.eventWorker = eventWorker;
    }

    @Override
    public void importPremierData() {
        List<Season> seasons = restService.getSeasons().data();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<SeasonEntity> seasonEntities = PremierImportHelperUtil.toSeasonEntity(seasons);

            List<SeasonEntity> managedSeasonEntities = importSeasonEntities(seasonEntities, session);

            List<EventEntity> eventEntities = PremierImportHelperUtil.toEventEntities(seasons, seasonEntities);

            importEventEntities(eventEntities,
                    managedSeasonEntities,
                    session);
        });
    }

    /**
     * Imports all season entities into the database.
     *
     * @param seasonEntities the list of seasons received from the API
     * @param session        the current database session
     * @return a list of all season entities managed by Hibernate
     */
    private List<SeasonEntity> importSeasonEntities(@NonNull List<SeasonEntity> seasonEntities,
                                                    @NonNull Session session) {
        List<SeasonEntity> seasonsToPersist = new ArrayList<>();
        List<SeasonEntity> managedSeasonEntities = new ArrayList<>();
        List<SeasonEntity> existingSeasonEntities = seasonWorker.getAll(session);


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
        return managedSeasonEntities;
    }

    /**
     * Imports all event entities into the database.
     *
     * @param eventEntities         the list of events received from the API
     * @param managedSeasonEntities season entities that are currently managed by hibernate
     * @param session               the current database session
     */
    private void importEventEntities(@NonNull List<EventEntity> eventEntities,
                                     @NonNull List<SeasonEntity> managedSeasonEntities,
                                     @NonNull Session session) {
        List<EventEntity> existingEventEntities = new ArrayList<>(eventWorker.getAll(session));
        List<EventEntity> eventsToPersist = new ArrayList<>();

        eventEntities.forEach(eventEntity -> {
            Optional<EventEntity> matchingExistingEntity = findMatchingExistingEntityOrThrow(eventEntity, existingEventEntities);

            if (matchingExistingEntity.isPresent()) {
                EventEntity existingEntity = matchingExistingEntity.get();
                existingEventEntities.remove(existingEntity);

                SeasonEntity managedSeason = findBySeasonExternalIdOrThrow(managedSeasonEntities, existingEntity);

                eventWorker.update(existingEntity,
                        eventEntity,
                        managedSeason);
            } else {
                SeasonEntity parentSeason = findBySeasonExternalIdOrThrow(managedSeasonEntities, eventEntity);

                eventEntity.setSeason(parentSeason);

                eventsToPersist.add(eventEntity);
            }
        });

        // Each non used existing entity can be deleted
        existingEventEntities.forEach(unusedEntity -> {
            unusedEntity.setSeason(null);
            session.remove(unusedEntity);
        });

        persistAll(session, eventsToPersist);
    }

    private static @NonNull Optional<EventEntity> findMatchingExistingEntityOrThrow(EventEntity eventEntity, List<EventEntity> existingEventEntities) {
        return existingEventEntities.stream()
                .filter(existingEntity -> existingEntity.equals(eventEntity))
                .findFirst();
    }

    private static @NonNull SeasonEntity findBySeasonExternalIdOrThrow(@NonNull List<SeasonEntity> managedSeasonEntities,
                                                                       @NonNull EventEntity existingEntity) {
        return managedSeasonEntities.stream()
                .filter(season -> season.getExternalId().equals(existingEntity.getSeason().getExternalId()))
                .findFirst()
                .orElseThrow();
    }

    private static <T> void persistAll(@NonNull Session session,
                                       @NonNull List<T> entities) {
        for (T entity : entities) {
            session.persist(entity);
        }
    }
}
