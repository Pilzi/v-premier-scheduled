package io.github.pilzi.database.enums;

import org.jspecify.annotations.NonNull;

public enum PollType {
    SCHEDULE("SCHEDULE"),
    AGENT("AGENT");

    @NonNull
    private final String text;

    PollType(@NonNull String text) {
        this.text = text;
    }
}
