package io.github.pilzi.discord.enums;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public enum Agent {
    ASTRA("Astra", Role.CONTROLLER, null),
    BREACH("Breach", Role.INITIATOR, null),
    BRIMSTONE("Brimstone", Role.CONTROLLER, null),
    CHAMBER("Chamber", Role.SENTINEL, null),
    CLOVE("Clove", Role.CONTROLLER, null),
    CYPHER("Cypher", Role.SENTINEL, null),
    DEADLOCK("Deadlock", Role.SENTINEL, null),
    FADE("Fade", Role.INITIATOR, null),
    GEKKO("Gekko", Role.INITIATOR, null),
    HARBOR("Harbor", Role.CONTROLLER, null),
    ISO("Iso", Role.DUELIST, null),
    JETT("Jett", Role.DUELIST, null),
    KAYO("KAY/O", Role.INITIATOR, null),
    KILLJOY("Killjoy", Role.SENTINEL, null),
    MIKS("Miks", Role.CONTROLLER, null),
    NEON("Neon", Role.DUELIST, null),
    OMEN("Omen", Role.CONTROLLER, null),
    PHOENIX("Phoenix", Role.DUELIST, null),
    RAZE("Raze", Role.DUELIST, null),
    REYNA("Reyna", Role.DUELIST, null),
    SAGE("Sage", Role.SENTINEL, null),
    SKYE("Skye", Role.INITIATOR, null),
    SOVA("Sova", Role.INITIATOR, null),
    TEJO("Tejo", Role.INITIATOR, null),
    VETO("Veto", Role.SENTINEL, null),
    VIPER("Viper", Role.CONTROLLER, null),
    VYSE("Vyse", Role.SENTINEL, null),
    WAYLAY("Waylay", Role.DUELIST, null),
    YORU("Yoru", Role.DUELIST, null),
    HUSH("Hush", Role.DUELIST, null);

    @NonNull
    private final String displayName;
    @NonNull
    private final Role role;
    @Nullable
    private final String emoji;

    Agent(@NonNull String displayName,
          @NonNull Role role,
          @Nullable String emoji) {
        this.displayName = displayName;
        this.role = role;
        this.emoji = emoji;
    }

    @NonNull
    public static Role getRoleByName(@NonNull String agentName) {
        String agentNameLowerCase = agentName.toLowerCase();
        for (Agent agent : Agent.values()) {
            if (agent.name().toLowerCase().equals(agentNameLowerCase)) {
                return agent.getRole();
            }
        }
        throw new RuntimeException(String.format("Unable to return role because the agent named %s does not exist", agentName));
    }

    @NonNull
    public static String getEmojiByNameOrDisplayNameFallback(@NonNull String agentName) {
        String agentNameLowerCase = agentName.toLowerCase();
        for (Agent agent : Agent.values()) {
            if (agent.name().toLowerCase().equals(agentNameLowerCase)) {
                return agent.getEmojiOrDisplayName();
            }
        }
        throw new RuntimeException(String.format("Unable to return emoji or displayName because the agent named %s does not exist", agentName));
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @return the matching defined emoji for the agent. The display name is used as a fallback.
     */
    @NonNull
    public String getEmojiOrDisplayName() {
        return this.emoji == null ? this.displayName : this.emoji;
    }

    @NonNull
    public Role getRole() {
        return this.role;
    }
}
