package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.ActivePollEntity;
import io.github.pilzi.database.enums.PollType;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActivePollWorker {
    @Nullable
    public ActivePollEntity getScheduledActivePoll(@NonNull Session session,
                                                   @NonNull Long discordGuildId) {
        return getActivePoll(session, discordGuildId, PollType.SCHEDULE);
    }

    @Nullable
    public ActivePollEntity getAgentActivePoll(@NonNull Session session,
                                               @NonNull Long discordGuildId) {
        return getActivePoll(session, discordGuildId, PollType.AGENT);
    }

    @Nullable
    private ActivePollEntity getActivePoll(@NonNull Session session,
                                           @NonNull Long discordGuildId,
                                           @Nullable PollType pollType) {
        return session.createQuery("SELECT activePoll" +
                        " FROM ActivePollEntity activePoll" +
                        " WHERE activePoll.pollType = :pollType" +
                        " AND activePoll.guild.discordId = :discordGuildId", ActivePollEntity.class)
                .setParameter("pollType", pollType)
                .setParameter("discordGuildId", discordGuildId)
                .getSingleResultOrNull();
    }

}
