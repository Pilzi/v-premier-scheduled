package io.github.pilzi.service.services;

import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface PollService {

    void handlePollsForGuilds(@NonNull List<Guild> guilds);

    void addScheduleVote(long userId, long answerId, long messageId);

    void removeScheduleVote(long userId, long answerId, long messageId);

    void importSelectedAgents(@NonNull Long guildId,
                              @NonNull Long messageId,
                              @NonNull Long discordUserId,
                              @NonNull List<String> selectedAgents);
}
