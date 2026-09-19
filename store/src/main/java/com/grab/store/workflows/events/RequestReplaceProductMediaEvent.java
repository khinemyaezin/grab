package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record RequestReplaceProductMediaEvent(
        String workflowId,
        String merchantId,
        String productId,
        List<Media> medias,
        Instant occurredAt,
        int version
) implements Event {

    public RequestReplaceProductMediaEvent {
        medias = medias == null ? List.of() : List.copyOf(medias);
    }

    public record Media(
            String id,
            String storageKey,
            String contentType,
            Integer rank
    ) {
    }
}
