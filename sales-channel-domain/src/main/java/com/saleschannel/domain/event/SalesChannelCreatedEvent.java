package com.saleschannel.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record SalesChannelCreatedEvent(
        String salesChannelId,
        String name,
        String type,
        String owner,
        String merchantId,
        String status,
        Instant occurredAt
) implements Event {
}
