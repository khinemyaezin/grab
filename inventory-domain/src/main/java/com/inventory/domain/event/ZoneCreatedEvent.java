package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;

import java.time.LocalDateTime;

public record ZoneCreatedEvent(
        Id zoneId,
        Id locationId,
        String code,
        String name,
        ZoneType type,
        LocalDateTime occurredAt
) implements Event {
}
