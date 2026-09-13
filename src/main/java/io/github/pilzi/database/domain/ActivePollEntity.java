package io.github.pilzi.database.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

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

    @NonNull
    @ManyToMany
    @JoinTable(
            name = "active_poll_event_reference",
            joinColumns = @JoinColumn(name = "active_poll_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<EventEntity> events;

    public ActivePollEntity(@NonNull Instant startAt,
                            @NonNull Instant endAt,
                            @NonNull GuildEntity guild,
                            @NonNull List<EventEntity> events) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.guild = guild;
        this.events = events;
    }

    protected ActivePollEntity(){}

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

    public @NonNull List<EventEntity> getEvents() {
        return events;
    }

    public void setEvents(@NonNull List<EventEntity> events) {
        this.events = events;
    }

    public @Nullable Long getMessageId() {
        return messageId;
    }

    public void setMessageId(@Nullable Long messageId) {
        this.messageId = messageId;
    }
}
