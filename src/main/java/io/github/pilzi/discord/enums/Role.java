package io.github.pilzi.discord.enums;

import org.jspecify.annotations.NonNull;

public enum Role {
    DUELIST("Duelist"),
    INITIATOR("Initiator"),
    CONTROLLER("Controller"),
    SENTINEL("Sentinel");

    @NonNull
    private final String displayName;

    Role(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }
}
