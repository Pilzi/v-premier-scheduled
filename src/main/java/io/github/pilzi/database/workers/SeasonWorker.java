package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.SeasonEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SeasonWorker {

    @NonNull
    public List<SeasonEntity> getAll(@NonNull Session session) {
        Query<SeasonEntity> query = session.createQuery("SELECT season FROM SeasonEntity season", SeasonEntity.class);
        return query.list();
    }

    /**
     * It's really unlikely for any season to change start and end date.
     * As the importer should not run very often it's still fine to update each entity that does already exist
     *
     * @param entityToUpdate     the entity that does already exist in database
     * @param entityToUpdateWith the newer updated entity
     */
    public void update(@NonNull SeasonEntity entityToUpdate, @NonNull SeasonEntity entityToUpdateWith) {
        entityToUpdate.setStartAt(entityToUpdateWith.getStartAt());
        entityToUpdate.setEndAt(entityToUpdateWith.getEndAt());
    }

    public void deleteAll(@NonNull Session session) {
        Query query = session.createQuery("DELETE SeasonEntity");
        query.executeUpdate();
    }

    public Optional<SeasonEntity> getFirst(Session session) {
        return session.createQuery("FROM SeasonEntity", SeasonEntity.class)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    public void persist(@NonNull Session session, @NonNull SeasonEntity unusedSeasonEntity) {
        session.persist(unusedSeasonEntity);
    }

    public Long count(@NonNull Session session) {
        return session.createQuery("SELECT COUNT(season) FROM SeasonEntity season", Long.class)
                .getSingleResult();
    }

    public Optional<SeasonEntity> find(@NonNull Session session, @NonNull UUID externalId) {
        return Optional.ofNullable(session.createQuery("SELECT season FROM SeasonEntity season WHERE season.externalId = :externalId", SeasonEntity.class)
                .setParameter("externalId", externalId)
                .getSingleResultOrNull());
    }
}
