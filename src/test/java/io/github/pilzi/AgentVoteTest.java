package io.github.pilzi;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.domain.AgentVoteEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.*;
import io.github.pilzi.discord.enums.Agent;
import io.github.pilzi.discord.enums.Role;
import io.github.pilzi.henrikdev.service.impl.MockRestService;
import io.github.pilzi.service.PollTestService;
import io.github.pilzi.service.impl.PollTestServiceImpl;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.services.impl.HibernateSessionFactoryTestServiceImpl;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import io.github.pilzi.service.services.impl.PollServiceImpl;
import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AgentVoteTest extends TestContainer {
    public static final long TEST_DISCORD_USER_ID = 1L;
    @NonNull
    private final SeasonWorker seasonWorker = new SeasonWorker();
    @NonNull
    private final ActivePollWorker activePollWorker = new ActivePollWorker();
    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;
    @NonNull
    private final AgentVoteWorker agentVoteWorker = new AgentVoteWorker();
    @NonNull
    final PollService pollService;
    @NonNull
    private final ImportService importService;
    @NonNull
    private final Guild mockGuild;
    @NonNull
    private final PollTestService pollTestService;

    AgentVoteTest() {
        hibernateSessionFactoryService = new HibernateSessionFactoryTestServiceImpl(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
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
    public void agent_vote_import_and_delete_test() {
        importService.importPremierData();
        pollTestService.createPollForMockGuild();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());

            List<String> selectedAgents = List.of(Agent.RAZE.getDisplayName());

            pollService.importSelectedAgents(
                    mockGuild.getIdLong(),
                    TEST_DISCORD_USER_ID,
                    selectedAgents,
                    Role.DUELIST);

            List<AgentVoteEntity> votesForUser = agentVoteWorker.getVotesForUser(session,
                    activePollEntity.getId(),
                    TEST_DISCORD_USER_ID);
            Assertions.assertEquals(selectedAgents.size(), votesForUser.size());

            agentVoteWorker.deleteVoteByRole(session, mockGuild.getIdLong(), Role.DUELIST, TEST_DISCORD_USER_ID);

            List<AgentVoteEntity> votesForUserAfterDelete = agentVoteWorker.getVotesForUser(session, activePollEntity.getId(), TEST_DISCORD_USER_ID);

            Assertions.assertEquals(0, votesForUserAfterDelete.size());
        });

        pollTestService.deleteActivePoll();
    }

    @Test
    public void agents_deleted_when_active_poll_deleted() {
        importService.importPremierData();
        pollTestService.createPollForMockGuild();

        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());

            AgentVoteEntity vote = new AgentVoteEntity(TEST_DISCORD_USER_ID, Agent.RAZE.getDisplayName(), Role.DUELIST, activePollEntity);

            session.persist(vote);
            session.flush();
            session.clear();

            ActivePollEntity poll = session.get(ActivePollEntity.class, activePollEntity.getId());
            session.remove(poll);
            session.flush();

            List<AgentVoteEntity> votesAfterActivePollDelete = agentVoteWorker.getVotesForUser(session,
                    activePollEntity.getId(),
                    TEST_DISCORD_USER_ID);

            Assertions.assertEquals(0, votesAfterActivePollDelete.size());
        });
    }
}