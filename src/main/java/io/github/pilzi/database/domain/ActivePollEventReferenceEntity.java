package io.github.pilzi.database.domain;

import io.github.pilzi.database.beans.ActivePollEventReferenceIdBean;
import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "active_poll_event_reference")
public class ActivePollEventReferenceEntity {

    @Nullable
    @EmbeddedId
    private ActivePollEventReferenceIdBean id;

    @NonNull
    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("activePollId")
    @JoinColumn(name = "active_poll_id", referencedColumnName = "id")
    private ActivePollEntity activePoll;

    @NonNull
    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @NonNull
    @Column(name = "order_index", nullable = false)
    private Long orderIndex;

    public ActivePollEventReferenceEntity(@NonNull ActivePollEntity activePoll,
                                          @NonNull EventEntity event,
                                          long orderIndex) {
        this.activePoll = activePoll;
        this.event = event;
        this.orderIndex = orderIndex;
        this.id = new ActivePollEventReferenceIdBean(activePoll.getId(), event.getId());
    }

    protected ActivePollEventReferenceEntity() {
    }

    public @NonNull ActivePollEntity getActivePoll() {
        return activePoll;
    }

    public void setActivePoll(@NonNull ActivePollEntity activePoll) {
        this.activePoll = activePoll;
    }

    public @NonNull EventEntity getEvent() {
        return event;
    }

    public void setEvent(@NonNull EventEntity event) {
        this.event = event;
    }

    public @NonNull Long getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(@NonNull Long orderIndex) {
        this.orderIndex = orderIndex;
    }

    public @Nullable ActivePollEventReferenceIdBean getId() {
        return id;
    }

    public void setId(@Nullable ActivePollEventReferenceIdBean id) {
        this.id = id;
    }
}
