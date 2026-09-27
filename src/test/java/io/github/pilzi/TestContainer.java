package io.github.pilzi;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

public abstract class TestContainer{
    @NonNull
    @Container
    @SuppressWarnings("resource")
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
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
}
