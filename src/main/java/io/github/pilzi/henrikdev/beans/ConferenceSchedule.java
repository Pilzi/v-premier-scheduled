package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ConferenceSchedule(@JsonProperty("conference") String conference,
                                 @JsonProperty("starts_at") Instant startsAt,
                                 @JsonProperty("ends_at") Instant endsAt) {
}
