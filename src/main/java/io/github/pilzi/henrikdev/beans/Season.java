package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Season(
        @NonNull @JsonProperty("id") UUID id,
        @NonNull @JsonProperty("championship_event_id") UUID championshipEventId,
        @NonNull @JsonProperty("starts_at") Instant startsAt,
        @NonNull @JsonProperty("ends_at") Instant endsAt,
        @NonNull @JsonProperty("enrollment_starts_at") Instant enrollmentStartsAt,
        @NonNull @JsonProperty("enrollment_ends_at") Instant enrollmentEndsAt,
        @NonNull @JsonProperty("events") List<Event> events,
        @NonNull @JsonProperty("scheduled_events") List<ScheduledEvent> scheduledEvents

) {
}