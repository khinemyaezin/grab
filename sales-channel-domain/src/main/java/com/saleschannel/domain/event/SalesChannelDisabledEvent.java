package com.saleschannel.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record SalesChannelDisabledEvent(
        String salesChannelId,
        String type,
        String merchantId,
        Instant occurredAt
) implements Event {
}
