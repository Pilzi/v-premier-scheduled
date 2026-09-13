package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.ActivePollEventReferenceEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActivePollEventReferenceWorker {
    @Nullable
    public ActivePollEventReferenceEntity findByMessageIdAndIndex(@NonNull Session session,
                                                                  @NonNull Long messageId,
                                                                  @NonNull Long orderIndex) {
        Query<ActivePollEventReferenceEntity> query = session.createQuery(
                        "SELECT activePollReference FROM ActivePollEventReferenceEntity activePollReference" +
                                " WHERE activePollReference.orderIndex = :orderIndex" +
                                " AND activePollReference.activePoll.messageId = :messageId", ActivePollEventReferenceEntity.class)
                .setParameter("orderIndex", orderIndex)
                .setParameter("messageId", messageId);

        return query.getSingleResultOrNull();
    }
}
