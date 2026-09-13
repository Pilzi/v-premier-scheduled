package io.github.pilzi.service.services;

import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface PollService {

    /**
     *  Create a new poll when there is no existing poll for the current week.
     */
    void handlePollForAllGuilds(@NonNull List<Guild> guilds);

    void addVote(long userId, long answerId, long messageId);

    void removeVote(long userId, long answerId, long messageId);
}
