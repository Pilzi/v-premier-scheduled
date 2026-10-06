package io.github.pilzi;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.henrikdev.enums.Conference;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import junit.framework.Assert;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ImportPremiereDataTest extends TestContainer {
    @NonNull
    private final ImportService importService;
    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;
    @NonNull
    private final EventWorker eventWorker = new EventWorker();
    @NonNull
    private final SeasonWorker seasonWorker = new SeasonWorker();

    ImportPremiereDataTest() {
        hibernateSessionFactoryService = new HibernateSessionFactoryTestServiceImpl(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        importService = new ImportServiceImpl(new SeasonWorker(), hibernateSessionFactoryService, eventWorker);
        importService.importPremierData();
    }

    @Test
    public void is_db_test_container_running() {
        Assertions.assertTrue(postgres.isRunning(), "The container should run!");
    }

    @RepeatedTest(5)
    public void does_import_work_after_running_multiple_times_without_crashing() {
        importService.importPremierData();
    }

    @Test
    public void does_import_work_after_deleting_all_events() {
        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            eventWorker.deleteAll(session);
            List<EventEntity> events = eventWorker.getAll(session);

            Assert.assertTrue(events.isEmpty());
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<EventEntity> events = eventWorker.getAll(session);

            Assert.assertFalse(events.isEmpty());
        });
    }

    @Test
    public void does_import_work_after_deleting_all_seasons() {
        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            seasonWorker.deleteAll(session);
            List<SeasonEntity> seasons = seasonWorker.getAll(session);

            Assert.assertTrue(seasons.isEmpty());
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            List<SeasonEntity> seasons = seasonWorker.getAll(session);

            Assert.assertFalse(seasons.isEmpty());
        });
    }

    @Test
    public void is_every_event_deleted_after_deleting_seasons() {
        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            List<EventEntity> eventsBeforeDelete = eventWorker.getAll(session);

            Assert.assertFalse(eventsBeforeDelete.isEmpty());

            seasonWorker.deleteAll(session);

            List<EventEntity> eventsAfterDelete = eventWorker.getAll(session);

            Assert.assertTrue(eventsAfterDelete.isEmpty());
        });
    }

    @Test
    public void does_the_importer_delete_unused_event_entities() {
        importService.importPremierData();

        List<EventEntity> eventEntitiesBeforeAddingUnusedEvent = new ArrayList<>();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            eventEntitiesBeforeAddingUnusedEvent.addAll(eventWorker.getAll(session));

            SeasonEntity seasonEntity = seasonWorker.getFirst(session).orElseThrow();

            EventEntity unusedEventEntity = new EventEntity("undefined", Instant.now(), Instant.now(), false, Conference.EU_DACH, seasonEntity);
            eventWorker.persist(session, unusedEventEntity);
            List<EventEntity> eventEntitiesAfterAddingUnusedEvent = eventWorker.getAll(session);

            Assert.assertEquals(eventEntitiesBeforeAddingUnusedEvent.size() + 1, eventEntitiesAfterAddingUnusedEvent.size());
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            Assert.assertEquals(eventEntitiesBeforeAddingUnusedEvent.size(), eventWorker.getAll(session).size());
        });
    }

    @Disabled // TODO this is not implemented yet
    @Test
    public void does_the_importer_delete_unused_season_entities() {
        importService.importPremierData();

        List<SeasonEntity> seasonEntitiesBeforeAddingUnusedSeason = new ArrayList<>();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            seasonEntitiesBeforeAddingUnusedSeason.addAll(seasonWorker.getAll(session));

            SeasonEntity unusedSeasonEntity = new SeasonEntity(UUID.randomUUID(), Instant.now(), Instant.now());
            seasonWorker.persist(session, unusedSeasonEntity);
            List<EventEntity> seasonEntitiesAfterAddingUnusedSeason = eventWorker.getAll(session);

            Assert.assertEquals(seasonEntitiesBeforeAddingUnusedSeason.size() + 1, seasonEntitiesAfterAddingUnusedSeason.size());
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            Assert.assertEquals(seasonEntitiesBeforeAddingUnusedSeason.size(), seasonWorker.getAll(session).size());
        });
    }

    @Test
    public void reimport_deleted_season_work() {
        importService.importPremierData();
        AtomicReference<Long> seasonCountBeforeRemove = new AtomicReference<>();


        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            SeasonEntity seasonEntity = seasonWorker.getFirst(session).orElseThrow();

            seasonCountBeforeRemove.set(seasonWorker.count(session));

            session.remove(seasonEntity);

            Long seasonCountAfterRemoveIncreasedByOne = seasonWorker.count(session) + 1;

            Assert.assertEquals(seasonCountBeforeRemove.get(), seasonCountAfterRemoveIncreasedByOne);
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            Assert.assertEquals( seasonCountBeforeRemove.get(), seasonWorker.count(session));
        });
    }

    @Test
    public void reimport_deleted_event_work() {
        importService.importPremierData();

        AtomicReference<Long> eventCountBeforeRemove = new AtomicReference<>();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            EventEntity eventEntity = eventWorker.getFirst(session).orElseThrow();

            eventCountBeforeRemove.set(eventWorker.count(session));

            session.remove(eventEntity);

            Long eventCountAfterRemoveIncreasedByOne = eventWorker.count(session) + 1;

            Assert.assertEquals(eventCountBeforeRemove.get(), eventCountAfterRemoveIncreasedByOne);
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            Assert.assertEquals( eventCountBeforeRemove.get(), eventWorker.count(session));
        });
    }

    @Test
    public void does_importer_detect_manipulated_field_for_event() {
        importService.importPremierData();

        AtomicReference<Long> entityId = new AtomicReference<>();


        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            EventEntity eventEntity = eventWorker.getFirst(session).orElseThrow();

            entityId.set(eventEntity.getId());

            eventEntity.setMap("test");
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            // The importer simply creates a new entity for manipulated fields so it should be not possible to find the old one
            Assert.assertFalse(eventWorker.find(session, entityId.get()).isPresent());
        });
    }

    @Test
    public void does_importer_detect_manipulated_field_for_season() {
        importService.importPremierData();

        AtomicReference<UUID> seasonId = new AtomicReference<>();
        AtomicReference<Instant> initialStartAt = new AtomicReference<>();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            SeasonEntity seasonEntity = seasonWorker.getFirst(session).orElseThrow();

            seasonId.set(seasonEntity.getExternalId());
            initialStartAt.set(seasonEntity.getStartAt());
            seasonEntity.setStartAt(Instant.ofEpochSecond(0));
        });

        importService.importPremierData();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {

            SeasonEntity matchingEntity = seasonWorker.find(session, seasonId.get()).orElseThrow();
            Assertions.assertEquals(initialStartAt.get(), matchingEntity.getStartAt());
        });
    }
}
