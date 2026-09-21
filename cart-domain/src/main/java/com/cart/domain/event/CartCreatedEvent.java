package com.cart.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record CartCreatedEvent(
        String cartId,
        String salesChannelId,
        String guestToken,
        Instant occurredAt
) implements Event {
}
