package io.github.pilzi.service;

import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;

public interface PollTestService {
    void deleteActivePoll();

    void createPollForMockGuild();

    void createEventsForCurrentWeek();

    @NonNull Guild getMockGuild();
}
