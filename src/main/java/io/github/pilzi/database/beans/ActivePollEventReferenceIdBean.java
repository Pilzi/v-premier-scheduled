package io.github.pilzi.database.beans;

import jakarta.persistence.Embeddable;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

@Embeddable
public class ActivePollEventReferenceIdBean implements Serializable {
    @NonNull
    private Long activePollId;
    @NonNull
    private Long eventId;

    protected ActivePollEventReferenceIdBean() {}

    public ActivePollEventReferenceIdBean(@NonNull Long activePollId, @NonNull Long eventId) {
        this.activePollId = activePollId;
        this.eventId = eventId;
    }
}
