package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.ActivePollVoteEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActivePollVoteWorker {

    @Nullable
    public ActivePollVoteEntity find(@NonNull Session session,
                                     @NonNull Long orderIndex,
                                     @NonNull Long messageId,
                                     @NonNull Long userId) {
        Query<ActivePollVoteEntity> query = session.createQuery(
                        "SELECT vote FROM ActivePollVoteEntity vote" +
                                " WHERE vote.activePollEventReference.orderIndex = :orderIndex" +
                                " AND vote.activePollEventReference.activePoll.messageId = :messageId" +
                                " AND vote.userId = :userId", ActivePollVoteEntity.class)
                .setParameter("orderIndex", orderIndex)
                .setParameter("messageId", messageId)
                .setParameter("userId", userId);

        return query.getSingleResultOrNull();
    }

}
