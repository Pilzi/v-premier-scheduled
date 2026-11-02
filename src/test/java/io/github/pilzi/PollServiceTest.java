package io.github.pilzi;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.*;
import io.github.pilzi.henrikdev.service.impl.MockRestService;
import io.github.pilzi.service.PollTestService;
import io.github.pilzi.service.impl.PollTestServiceImpl;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import io.github.pilzi.service.services.impl.PollServiceImpl;
import junit.framework.Assert;
import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class PollServiceTest extends TestContainer {
    @NonNull
    private final SeasonWorker seasonWorker = new SeasonWorker();
    @NonNull
    private final ActivePollWorker activePollWorker = new ActivePollWorker();
    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;
    @NonNull
    final PollService pollService;
    @NonNull
    private final ImportService importService;
    @NonNull
    private final PollTestService pollTestService;
    @NonNull
    private final Guild mockGuild;

    PollServiceTest() {
        hibernateSessionFactoryService = new HibernateSessionFactoryTestServiceImpl(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        AgentVoteWorker agentVoteWorker = new AgentVoteWorker();
        GuildWorker guildWorker = new GuildWorker();
        EventWorker eventWorker = new EventWorker();
        pollService = new PollServiceImpl(
                eventWorker,
                guildWorker,
                activePollWorker,
                hibernateSessionFactoryService,
                agentVoteWorker);
        pollTestService = new PollTestServiceImpl(hibernateSessionFactoryService, pollService, seasonWorker, activePollWorker);
        mockGuild = pollTestService.getMockGuild();
        importService = new ImportServiceImpl(new MockRestService(), new SeasonWorker(), hibernateSessionFactoryService, eventWorker);
    }

    @Test
    public void poll_creation_for_guild_without_poll() {
        importService.importPremierData();
        pollTestService.createPollForMockGuild();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            Assert.assertNotNull(activePollEntity);
        });

        pollTestService.deleteActivePoll();
    }

    @Test
    public void poll_creation_after_poll_delete_on_reimport() {
        importService.importPremierData();
        pollTestService.createPollForMockGuild();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            Assert.assertNotNull(activePollEntity);
            session.remove(activePollEntity);
            session.flush();
            ActivePollEntity activePollEntityAfterDelete = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            Assertions.assertNull(activePollEntityAfterDelete);
        });

        pollService.handleScheduledPollForAllGuilds(List.of(mockGuild));

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            Assert.assertNotNull(activePollEntity);
        });

        pollTestService.deleteActivePoll();
    }

    @Test
    public void delete_expired_active_poll() {
        importService.importPremierData();
        pollTestService.createPollForMockGuild();
        AtomicLong expiredPollId = new AtomicLong();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = Optional.ofNullable(activePollWorker.getActivePoll(session, mockGuild.getIdLong())).orElseThrow();
            activePollEntity.setStartAt(Instant.ofEpochSecond(0));
            activePollEntity.setEndAt(Instant.ofEpochSecond(0));
            expiredPollId.set(activePollEntity.getId());
        });

        pollService.handleScheduledPollForAllGuilds(List.of(mockGuild));

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            Assertions.assertNotEquals(expiredPollId.get(), activePollEntity.getId());
        });

        pollTestService.deleteActivePoll();
    }
}
