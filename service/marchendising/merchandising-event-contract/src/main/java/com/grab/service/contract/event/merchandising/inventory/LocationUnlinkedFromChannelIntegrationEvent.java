package com.grab.service.contract.event.merchandising.inventory;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record LocationUnlinkedFromChannelIntegrationEvent(
        String locationId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
