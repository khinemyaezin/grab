package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record BinCreatedEvent(
        Id binId,
        Id zoneId,
        String code,
        String name,
        Integer maxCapacity,
        LocalDateTime occurredAt
) implements Event {
}
