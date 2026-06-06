package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.LocationType;

import java.time.LocalDateTime;

public record LocationCreatedEvent(
        Id locationId,
        String code,
        String name,
        LocationType type,
        LocalDateTime occurredAt
) implements Event {
}
