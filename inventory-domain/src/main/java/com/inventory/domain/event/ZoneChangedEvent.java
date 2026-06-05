package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;

import java.time.LocalDateTime;

public record ZoneChangedEvent(
        Id locationId,
        Id zoneId,
        String zoneCode,
        ZoneType zoneType,
        ChangeType changeType,
        LocalDateTime occurredAt
) implements Event {

    public enum ChangeType {
        ADDED, UPDATED, REMOVED
    }
}
