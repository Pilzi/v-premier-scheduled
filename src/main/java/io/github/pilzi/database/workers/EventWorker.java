package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.service.utils.CalendarUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventWorker {

    @NonNull
    public List<EventEntity> getAll(@NonNull Session session) {
        Query<EventEntity> query = session.createQuery("SELECT event FROM EventEntity event", EventEntity.class);
        return query.list();
    }

    public void update(@NonNull EventEntity entityToUpdate,
                       @NonNull EventEntity entityToUpdateWith,
                       @NonNull SeasonEntity season) {
        entityToUpdate.setConference(entityToUpdateWith.getConference());
        entityToUpdate.setEndAt(entityToUpdateWith.getEndAt());
        entityToUpdate.setMap(entityToUpdateWith.getMap());
        entityToUpdate.setSeason(season);
        entityToUpdate.setStartAt(entityToUpdateWith.getStartAt());
    }

    @NonNull
    public List<EventEntity> collectEventsForCurrentWeek(@NonNull Session session) {
        return new ArrayList<>(this.getAll(session).stream()
                .filter(eventEntity -> CalendarUtil.isInCurrentWeek(eventEntity.getStartAt(), eventEntity.getEndAt()))
                .toList());
    }

    public void deleteAll(@NonNull Session session) {
        Query query = session.createQuery("DELETE EventEntity");
        query.executeUpdate();
    }

    public void persist(@NonNull Session session, @NonNull EventEntity eventEntity) {
        session.persist(eventEntity);
    }

    @NonNull
    public Optional<EventEntity> getFirst(@NonNull Session session) {
        return session.createQuery("FROM EventEntity ", EventEntity.class)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    @NonNull
    public Long count(@NonNull Session session) {
        return session.createQuery("SELECT COUNT(event) FROM EventEntity event", Long.class)
                .getSingleResult();
    }

    public Optional<EventEntity> find(@NonNull Session session, @NonNull Long entityId) {
        return Optional.ofNullable(session.createQuery("SELECT event FROM EventEntity event WHERE event.id = :searchedId", EventEntity.class)
                .setParameter("searchedId", entityId)
                .getSingleResultOrNull());
    }
}
