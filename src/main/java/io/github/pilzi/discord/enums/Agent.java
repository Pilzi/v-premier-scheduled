package io.github.pilzi.discord.enums;

import org.jspecify.annotations.NonNull;

public enum Agent {
    ASTRA("Astra", Role.CONTROLLER),
    BREACH("Breach", Role.INITIATOR),
    BRIMSTONE("Brimstone", Role.CONTROLLER),
    CHAMBER("Chamber", Role.SENTINEL),
    CLOVE("Clove", Role.CONTROLLER),
    CYPHER("Cypher", Role.SENTINEL),
    DEADLOCK("Deadlock", Role.SENTINEL),
    FADE("Fade", Role.INITIATOR),
    GEKKO("Gekko", Role.INITIATOR),
    HARBOR("Harbor", Role.CONTROLLER),
    ISO("Iso", Role.DUELIST),
    JETT("Jett", Role.DUELIST),
    KAYO("KAY/O", Role.INITIATOR),
    KILLJOY("Killjoy", Role.SENTINEL),
    MIKS("Miks", Role.CONTROLLER),
    NEON("Neon", Role.DUELIST),
    OMEN("Omen", Role.CONTROLLER),
    PHOENIX("Phoenix", Role.DUELIST),
    RAZE("Raze", Role.DUELIST),
    REYNA("Reyna", Role.DUELIST),
    SAGE("Sage", Role.SENTINEL),
    SKYE("Skye", Role.INITIATOR),
    SOVA("Sova", Role.INITIATOR),
    TEJO("Tejo", Role.INITIATOR),
    VETO("Veto", Role.SENTINEL),
    VIPER("Viper", Role.CONTROLLER),
    VYSE("Vyse", Role.SENTINEL),
    WAYLAY("Waylay", Role.DUELIST),
    YORU("Yoru", Role.DUELIST);

    @NonNull
    private final String displayName;
    @NonNull
    private final Role role;

    Agent(@NonNull String displayName,@NonNull Role role) {
        this.displayName = displayName;
        this.role = role;
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NonNull
    public Role getRole() {
        return this.role;
    }
}
