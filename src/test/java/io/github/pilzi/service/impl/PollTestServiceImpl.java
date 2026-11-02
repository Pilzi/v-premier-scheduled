package io.github.pilzi.service.impl;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.database.utils.TransactionalUtil;
import io.github.pilzi.database.workers.ActivePollWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.henrikdev.enums.Conference;
import io.github.pilzi.service.PollTestService;
import io.github.pilzi.service.services.HibernateSessionFactoryService;
import io.github.pilzi.service.services.PollService;
import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

public class PollTestServiceImpl implements PollTestService {
    @NonNull
    private final Guild mockGuild = createMockGuild();
    @NonNull
    private final HibernateSessionFactoryService hibernateSessionFactoryService;
    @NonNull
    private final PollService pollService;
    @NonNull
    private final SeasonWorker seasonWorker;
    @NonNull
    private final ActivePollWorker activePollWorker;

    public PollTestServiceImpl(@NonNull HibernateSessionFactoryService hibernateSessionFactoryService,
                        @NonNull PollService pollService,
                        @NonNull SeasonWorker seasonWorker,
                        @NonNull ActivePollWorker activePollWorker) {
        this.hibernateSessionFactoryService = hibernateSessionFactoryService;
        this.pollService = pollService;
        this.seasonWorker = seasonWorker;
        this.activePollWorker = activePollWorker;
    }

    @Override
    public void deleteActivePoll() {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            ActivePollEntity activePollEntity = activePollWorker.getActivePoll(session, mockGuild.getIdLong());
            session.remove(activePollEntity);
        });
    }

    @Override
    public void createPollForMockGuild() {
        createEventsForCurrentWeek();
        pollService.handleScheduledPollForAllGuilds(List.of(mockGuild));
    }

    @Override
    public void createEventsForCurrentWeek() {
        TransactionalUtil.executeInTransaction(hibernateSessionFactoryService, session -> {
            SeasonEntity season = seasonWorker.getFirst(session).orElseThrow();
            session.persist(new EventEntity("test",
                    Instant.now(),
                    Instant.now(),
                    false,
                    Conference.EU_DACH,
                    season));
        });
    }

    @Override
    public @NonNull Guild getMockGuild() {
        return mockGuild;
    }


    private @NonNull Guild createMockGuild() {
        Guild mockGuild = Mockito.mock(Guild.class);
        Mockito.when(mockGuild.getIdLong()).thenReturn(1L);
        Mockito.when(mockGuild.getId()).thenReturn("1");
        Mockito.when(mockGuild.getName()).thenReturn("test");
        return mockGuild;
    }
}
