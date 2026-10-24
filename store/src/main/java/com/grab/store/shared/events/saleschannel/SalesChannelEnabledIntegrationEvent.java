package com.grab.store.shared.events.saleschannel;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record SalesChannelEnabledIntegrationEvent(
        String salesChannelId,
        String type,
        String merchantId,
        Instant occurredAt,
        int version
) implements Event {
}
