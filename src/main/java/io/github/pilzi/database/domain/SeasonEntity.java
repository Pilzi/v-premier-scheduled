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
    @Column(name = "season_id")
    private UUID seasonId;

    @NonNull
    @Column(name = "start_at")
    private Instant startAt;

    @NonNull
    @Column(name = "end_at")
    private Instant endAt;

    public SeasonEntity(@NonNull UUID seasonId,
                        @NonNull Instant startAt,
                        @NonNull Instant endAt) {
        this.seasonId = seasonId;
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

    public @NonNull UUID getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(@NonNull UUID seasonId) {
        this.seasonId = seasonId;
    }
}
