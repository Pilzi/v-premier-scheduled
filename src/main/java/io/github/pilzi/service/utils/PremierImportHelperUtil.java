package io.github.pilzi.service.utils;

import io.github.pilzi.database.domain.EventEntity;
import io.github.pilzi.database.domain.SeasonEntity;
import io.github.pilzi.henrikdev.beans.Event;
import io.github.pilzi.henrikdev.beans.Map;
import io.github.pilzi.henrikdev.beans.ScheduledEvent;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.enums.Conference;
import org.jspecify.annotations.NonNull;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class PremierImportHelperUtil {
    @NonNull
    public static final String TOURNAMENT_EVENT_TYPE = "TOURNAMENT";
    public static final int REGULAR_PREMIER_WEEK_IN_HOURS = 1;
    // Scrim event are always practice matches the match result won't affect the actual team standing in the season
    public static final String SCRIM_EVENT_TYPE = "SCRIM";

    @NonNull
    public static List<SeasonEntity> toSeasonEntity(@NonNull List<Season> seasons) {

        return seasons.stream().map(season -> new SeasonEntity(
                        season.id(),
                        season.startsAt(),
                        season.startsAt()
                ))
                .toList();
    }

    @NonNull
    public static List<EventEntity> toEventEntities(@NonNull List<Season> seasons,
                                                    @NonNull List<SeasonEntity> seasonEntities) {
        return seasons.stream().filter(season -> !season.scheduledEvents().isEmpty())
                .flatMap(season -> collectEvent(
                        season.events(),
                        season.scheduledEvents(),
                        findMatchingSeasonEntity(seasonEntities, season)
                ).stream())
                .toList();
    }

    private static @NonNull SeasonEntity findMatchingSeasonEntity(@NonNull List<SeasonEntity> seasonEntities, @NonNull Season season) {
        return seasonEntities.stream()
                .filter(entity -> entity.getExternalId().equals(season.id()))
                .findFirst()
                .orElseThrow();
    }

    @NonNull
    private static Set<EventEntity> collectEvent(@NonNull List<Event> events,
                                                 @NonNull List<ScheduledEvent> scheduledEvents,
                                                 @NonNull SeasonEntity seasonEntity) {

        if (scheduledEvents.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Event> typeFilteredEvents = events.stream().filter(event -> !event.type().equals(TOURNAMENT_EVENT_TYPE)).collect(Collectors.toSet());

        Set<ScheduledEvent> eventsMatchingConference = new HashSet<>();

        for (ScheduledEvent scheduledEvent : scheduledEvents) {
            if (scheduledEvent.conference().equals(Conference.EU_DACH)) {
                eventsMatchingConference.add(scheduledEvent);
            }
        }

        // TODO add playoffs (type "TOURNAMENT") its currently not possible to store multiple maps in database for a single event
        Set<EventEntity> mergedEventEntities = eventsMatchingConference.stream()
                .filter(PremierImportHelperUtil::isEventOneHourLong)
                .map(scheduledEvent -> {
                    Optional<Event> matchingEvent = typeFilteredEvents.stream().filter(event -> event.id().equals(scheduledEvent.id())).findFirst();

                    return matchingEvent.map(event -> new EventEntity(
                                    event.mapSelection().maps().stream().map(Map::name).findFirst().orElseThrow(),
                                    scheduledEvent.startsAt(),
                                    scheduledEvent.endsAt(),
                                    event.type().equals(SCRIM_EVENT_TYPE),
                                    scheduledEvent.conference(),
                                    seasonEntity
                            ))
                            .orElse(null);

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        return filterPracticeMatchesIfThereIsAlreadyAnMatchAtTheSameTime(mergedEventEntities);
    }

    /**
     * Some conferences have both a practice and a match scheduled at the exact same time
     * for the same map (this happens on the day a team's schedule transitions from the
     * practice phase to the match phase — the last practice slot and the first match slot
     * can land on the same day/time). In that case, only the match should count, so the
     * practice entry is dropped.
     *
     * @param mergedEventEntities scheduled events merged into an eventEntity
     * @return Set of entities with dropped practice matches scheduled at the exact same time
     */
    private static @NonNull Set<EventEntity> filterPracticeMatchesIfThereIsAlreadyAnMatchAtTheSameTime(Set<EventEntity> mergedEventEntities) {
        return mergedEventEntities.stream().map(event -> {
            List<EventEntity> matchingEvents = mergedEventEntities.stream()
                    .filter(mergedEvent -> mergedEvent.getStartAt().equals(event.getStartAt())
                            && mergedEvent.getEndAt().equals(event.getEndAt())
                            && mergedEvent.getMap().equals(event.getMap())
                            && mergedEvent.getConference().equals(event.getConference()))
                    .toList();

            if (matchingEvents.size() > 1) {
                return matchingEvents.stream().filter(matchingEvent -> !matchingEvent.isPractice()).findAny().orElse(event);
            }

            return event;
        }).collect(Collectors.toSet());
    }

    /**
     * Checks whether a scheduled Premier event corresponds to the regular one-hour time slot.
     * <p>
     * Normal Premier games are scheduled in one-hour windows. Some events
     * appear to use a shorter 15-minute window instead, which based on current observations
     * seems tied to higher divisions.
     *
     * @param scheduledEvent the scheduled event to check, containing {@code startsAt} and
     *                       {@code endsAt} timestamps
     * @return {@code true} if the event spans exactly one hour (regular slot);
     * {@code false} if it spans a shorter window (e.g. 15 minutes)
     */
    private static boolean isEventOneHourLong(ScheduledEvent scheduledEvent) {
        return scheduledEvent.endsAt().minus(REGULAR_PREMIER_WEEK_IN_HOURS, ChronoUnit.HOURS).equals(scheduledEvent.startsAt());
    }

}
