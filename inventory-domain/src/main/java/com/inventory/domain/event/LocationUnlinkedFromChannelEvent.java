package com.inventory.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record LocationUnlinkedFromChannelEvent(
        Id locationId,
        Id salesChannelId,
        LocalDateTime occurredAt
) implements Event {
}
