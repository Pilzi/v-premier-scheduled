package io.github.pilzi.henrikdev.enums;

import org.jspecify.annotations.NonNull;

public enum Conference {
    EU_DACH("EU_DACH"),
    EU_DACH_SUPER("EU_DACH_SUPER"),
    EU_EAST("EU_EAST"),
    EU_EAST_SUPER("EU_EAST_SUPER"),
    EU_FRANCE("EU_FRANCE"),
    EU_FRANCE_SUPER("EU_FRANCE_SUPER"),
    EU_IBIT("EU_IBIT"),
    EU_IBIT_SUPER("EU_IBIT_SUPER"),
    EU_MIDDLE_EAST("EU_MIDDLE_EAST"),
    EU_MIDDLE_EAST_SUPER("EU_MIDDLE_EAST_SUPER"),
    EU_NORTH("EU_NORTH"),
    EU_NORTH_SUPER("EU_NORTH_SUPER"),
    EU_TURKEY("EU_TURKEY"),
    EU_TURKEY_SUPER("EU_TURKEY_SUPER");

    @NonNull
    private String conference;

    Conference(@NonNull String conference) {
        this.conference = conference;
    }
}
