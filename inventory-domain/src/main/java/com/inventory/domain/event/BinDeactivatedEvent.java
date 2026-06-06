package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record BinDeactivatedEvent(
        Id binId,
        Id zoneId,
        LocalDateTime occurredAt
) implements Event {
}
