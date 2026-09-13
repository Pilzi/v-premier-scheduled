package io.github.pilzi.database.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Entity
@Table(name = "guild")
public class GuildEntity {

    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "discord_id", nullable = false, unique = true, length = 30)
    private String discordId;

    @NonNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Nullable
    @OneToOne(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivePollEntity activePoll;

    public GuildEntity(@NonNull String discordId,
                       @NonNull String name) {
        this.discordId = discordId;
        this.name = name;
    }

    protected GuildEntity(){}

    public @NonNull String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(@NonNull String discordId) {
        this.discordId = discordId;
    }

    public @NonNull String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public @Nullable ActivePollEntity getActivePoll() {
        return activePoll;
    }

    public void setActivePoll(@Nullable ActivePollEntity activePoll) {
        this.activePoll = activePoll;
    }
}
