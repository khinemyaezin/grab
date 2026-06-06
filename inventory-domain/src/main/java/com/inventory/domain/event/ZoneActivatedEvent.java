package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record ZoneActivatedEvent(
        Id zoneId,
        Id locationId,
        LocalDateTime occurredAt
) implements Event {
}
