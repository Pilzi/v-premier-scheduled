package io.github.pilzi.database.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "active_poll")
public class ActivePollEntity {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NonNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Nullable
    @Column(name = "message_id", unique = true)
    private Long messageId;

    @NonNull
    @OneToOne
    @JoinColumn(name = "guild_id", nullable = false, unique = true)
    private GuildEntity guild;

    public ActivePollEntity(@NonNull Instant startAt,
                            @NonNull Instant endAt,
                            @NonNull GuildEntity guild) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.guild = guild;
    }

    protected ActivePollEntity() {}

    public @NonNull Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(@NonNull Instant startAt) {
        this.startAt = startAt;
    }

    public @NonNull Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(@NonNull Instant endAt) {
        this.endAt = endAt;
    }

    public @NonNull GuildEntity getGuild() {
        return guild;
    }

    public void setGuild(@NonNull GuildEntity guild) {
        this.guild = guild;
    }

    public @Nullable Long getMessageId() {
        return messageId;
    }

    public void setMessageId(@Nullable Long messageId) {
        this.messageId = messageId;
    }

    public @Nullable Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }
}
