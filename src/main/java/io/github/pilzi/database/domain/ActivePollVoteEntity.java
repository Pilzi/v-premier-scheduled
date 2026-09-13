package io.github.pilzi.database.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        name = "active_poll_vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"active_poll_id", "event_id", "user_id"})
)
public class ActivePollVoteEntity {

    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "user_id")
    private Long userId;

    @NonNull
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "active_poll_id", referencedColumnName = "active_poll_id", nullable = false, updatable = false),
            @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false, updatable = false)
    })
    private ActivePollEventReferenceEntity activePollEventReference;

    public ActivePollVoteEntity(@NonNull Long userId, @NonNull ActivePollEventReferenceEntity activePollEventReference) {
        this.userId = userId;
        this.activePollEventReference = activePollEventReference;
    }

    protected ActivePollVoteEntity() {
    }

    public @NonNull Long getUserId() {
        return userId;
    }

    public void setUserId(@NonNull Long userId) {
        this.userId = userId;
    }

    public @NonNull ActivePollEventReferenceEntity getActivePollEventReference() {
        return activePollEventReference;
    }

    public void setActivePollEventReference(@NonNull ActivePollEventReferenceEntity activePollEventReference) {
        this.activePollEventReference = activePollEventReference;
    }

    public @Nullable Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }
}