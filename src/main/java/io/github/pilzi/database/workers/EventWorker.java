package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.service.utils.CalendarUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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
                .filter(this::isEventInCurrentWeek)
                .toList());
        }

        private boolean isEventInCurrentWeek(@NonNull EventEntity event) {
            LocalDate firstDayOfCurrentWeek = CalendarUtil.getFirstDayOfCurrentWeek();
            LocalDate lastDayOfCurrentWeek = CalendarUtil.getLastDayOfCurrentWeek();

            return LocalDate.ofInstant(event.getStartAt(), ZoneId.systemDefault()).isAfter(firstDayOfCurrentWeek)
                    && LocalDate.ofInstant(event.getEndAt(), ZoneId.systemDefault()).isBefore(lastDayOfCurrentWeek);
        }
    }
