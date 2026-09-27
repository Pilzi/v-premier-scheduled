package io.github.pilzi;

import io.github.pilzi.database.workers.*;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

public class ImportPremiereDataTest {
    @NonNull
    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("src/main/java/io/github/pilzi/database/scripts/init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
            );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    ImportPremiereDataTest() {
        ImportService importService = new ImportServiceImpl(new SeasonWorker(),
                new HibernateSessionFactoryTestServiceImpl(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()),
                new EventWorker());
        importService.importPremierData();
    }

    @Test
    public void is_db_test_container_running() {
        Assertions.assertTrue(postgres.isRunning(), "The container should run!");
    }
}
