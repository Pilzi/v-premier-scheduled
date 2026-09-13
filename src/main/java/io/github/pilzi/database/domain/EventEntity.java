package io.github.pilzi.database.domain;

import io.github.pilzi.henrikdev.enums.Conference;
import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "event")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "map", nullable = false, length = 20)
    private String map;

    @NonNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NonNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @NonNull
    @Column(name = "is_practice", nullable = false)
    private Boolean isPractice;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "conference", nullable = false, length = 20)
    private Conference conference;

    @Nullable
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "season_id", referencedColumnName = "id", nullable = false)
    private SeasonEntity season;

    public EventEntity(@NonNull String map,
                       @NonNull Instant startAt,
                       @NonNull Instant endAt, @NonNull Boolean isPractice,
                       @NonNull Conference conference,
                       @NonNull SeasonEntity season) {
        this.map = map;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isPractice = isPractice;
        this.conference = conference;
        this.season = season;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EventEntity that = (EventEntity) o;
        return Objects.equals(map, that.map)
                && Objects.equals(startAt, that.startAt)
                && Objects.equals(endAt, that.endAt)
                && Objects.equals(isPractice, that.isPractice)
                && Objects.equals(season.getExternalId(), that.season.getExternalId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, startAt, endAt, isPractice, conference, season);
    }

    protected EventEntity() {
    }


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

    public @NonNull Conference getConference() {
        return conference;
    }

    public void setConference(@NonNull Conference conference) {
        this.conference = conference;
    }

    public @NonNull String getMap() {
        return map;
    }

    public void setMap(@NonNull String map) {
        this.map = map;
    }

    public @Nullable SeasonEntity getSeason() {
        return season;
    }

    public void setSeason(@Nullable SeasonEntity season) {
        this.season = season;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NonNull Boolean isPractice() {
        return isPractice;
    }

    public void setIsPractice(@NonNull Boolean isPractice) {
        this.isPractice = isPractice;
    }
}
