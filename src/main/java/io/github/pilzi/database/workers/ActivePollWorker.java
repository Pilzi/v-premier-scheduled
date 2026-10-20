package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.ActivePollEntity;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActivePollWorker {
    @Nullable
    public ActivePollEntity getScheduledActivePoll(@NonNull Session session,
                                                   @NonNull Long discordGuildId) {
        return getActivePoll(session, discordGuildId);
    }

    @Nullable
    private ActivePollEntity getActivePoll(@NonNull Session session,
                                           @NonNull Long discordGuildId) {
        return session.createQuery("SELECT activePoll" +
                        " FROM ActivePollEntity activePoll" +
                        " WHERE activePoll.guild.discordId = :discordGuildId", ActivePollEntity.class)
                .setParameter("discordGuildId", discordGuildId)
                .getSingleResultOrNull();
    }
}
