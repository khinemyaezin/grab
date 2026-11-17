package com.grab.service.contract.event.merchandising.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductUnpublishedFromChannelIntegrationEvent(
        String variantId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
