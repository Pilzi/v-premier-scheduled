package io.github.pilzi;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import junit.framework.Assert;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    // TODO test against external api
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
}
