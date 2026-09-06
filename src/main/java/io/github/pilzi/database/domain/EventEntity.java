package io.github.pilzi.database.domain;

import io.github.pilzi.henrikdev.enums.Conference;
import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "event")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "map")
    private String map;

    @NonNull
    @Column(name = "start_at")
    private Instant startAt;

    @NonNull
    @Column(name = "end_at")
    private Instant endAt;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "conference")
    private Conference conference;

    @NonNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "season_id", referencedColumnName = "id")
    private SeasonEntity season;

    public EventEntity(@NonNull String map,
                       @NonNull Instant startAt,
                       @NonNull Instant endAt,
                       @NonNull Conference conference,
                       @NonNull SeasonEntity season) {
        this.map = map;
        this.startAt = startAt;
        this.endAt = endAt;
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
                && conference == that.conference
                && Objects.equals(season, that.season);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, startAt, endAt, conference, season);
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

    public @NonNull SeasonEntity getSeason() {
        return season;
    }

    public void setSeason(@NonNull SeasonEntity season) {
        this.season = season;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
