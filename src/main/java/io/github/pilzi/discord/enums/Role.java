package io.github.pilzi.discord.enums;

import org.jspecify.annotations.NonNull;

import static io.github.pilzi.discord.utils.Message.DropdownUtil.AGENT_SELECTION_CUSTOM_ID_PREFIX;

public enum Role {
    DUELIST("DUELIST"),
    INITIATOR("INITIATOR"),
    CONTROLLER("CONTROLLER"),
    SENTINEL("SENTINEL");

    @NonNull
    private final String displayName;

    Role(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NonNull
    public static Role getRoleByCustomComponentId(@NonNull String customId) {
        for (Role role : Role.values()) {
            if (role.name().toLowerCase().equals(customId.split(AGENT_SELECTION_CUSTOM_ID_PREFIX)[1])) {
                return role;
            }
        }
        throw new RuntimeException("Unable to find matching role for id: " + customId);
    }
}
