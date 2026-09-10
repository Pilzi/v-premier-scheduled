package io.github.pilzi.database.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "season")
public class SeasonEntity {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "external_id", unique = true, nullable = false)
    private UUID externalId;

    @NonNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NonNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    public SeasonEntity(@NonNull UUID externalId,
                        @NonNull Instant startAt,
                        @NonNull Instant endAt) {
        this.externalId = externalId;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    protected SeasonEntity() {}


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

    public @Nullable Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    public @NonNull UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(@NonNull UUID seasonId) {
        this.externalId = seasonId;
    }
}
