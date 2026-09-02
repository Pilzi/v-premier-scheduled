package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Event(@NonNull @JsonProperty("id") UUID id,
                    @NonNull @JsonProperty("type") String type,
                    @NonNull @JsonProperty("starts_at") Instant startsAt,
                    @NonNull @JsonProperty("ends_at") Instant endsAt,
                    @NonNull @JsonProperty("conference_schedules") List<ConferenceSchedule> conferenceSchedules,
                    @NonNull @JsonProperty("map_selection") MapSelection mapSelection,
                    @NonNull @JsonProperty("points_required_to_participate") Long pointsRequiredToParticipate) {
}
