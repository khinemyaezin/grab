package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductStatusChangedEvent(
        Id productId,
        String oldStatus,
        String newStatus
) implements Event {

}
