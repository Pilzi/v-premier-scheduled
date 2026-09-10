package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.SeasonEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;

import java.util.List;

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
     * @param entityToUpdate the entity that does already exist in database
     * @param entityToUpdateWith the newer updated entity
     */
    public void update(@NonNull SeasonEntity entityToUpdate, @NonNull SeasonEntity entityToUpdateWith) {
        entityToUpdate.setStartAt(entityToUpdateWith.getStartAt());
        entityToUpdate.setEndAt(entityToUpdateWith.getEndAt());
    }
}
