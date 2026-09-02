package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record MapSelection(@NonNull @JsonProperty("type") String type,
                           @NonNull @JsonProperty("maps") List<Map> maps
) {
}
