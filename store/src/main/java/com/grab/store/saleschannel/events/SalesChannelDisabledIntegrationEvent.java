package com.grab.store.saleschannel.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record SalesChannelDisabledIntegrationEvent(
        String salesChannelId,
        String type,
        String merchantId,
        Instant occurredAt,
        int version
) implements Event {
}
