package io.github.pilzi;

import io.github.pilzi.database.workers.*;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImportPremiereDataTest extends TestContainer{

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
