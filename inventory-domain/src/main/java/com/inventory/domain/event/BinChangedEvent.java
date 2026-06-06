package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record BinChangedEvent(
        Id locationId,
        Id zoneId,
        Id binId,
        String binCode,
        ChangeType changeType,
        LocalDateTime occurredAt
) implements Event {

    public enum ChangeType {
        ADDED, UPDATED, REMOVED
    }
}
