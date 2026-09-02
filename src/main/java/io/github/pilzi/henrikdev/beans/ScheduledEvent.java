package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.pilzi.henrikdev.enums.Conference;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.UUID;

public record ScheduledEvent(@NonNull @JsonProperty("event_id") UUID id,
                             @NonNull @JsonProperty("starts_at") Instant startsAt,
                             @NonNull @JsonProperty("ends_at") Instant endsAt,
                             @NonNull @JsonProperty("conference") Conference conference)  {
}
