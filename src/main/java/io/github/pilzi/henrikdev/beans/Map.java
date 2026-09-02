package io.github.pilzi.henrikdev.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record Map(@NonNull @JsonProperty("name") String name,
                  @NonNull @JsonProperty("id") UUID id) {
}
