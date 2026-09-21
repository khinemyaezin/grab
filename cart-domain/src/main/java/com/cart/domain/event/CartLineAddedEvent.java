package com.cart.domain.event;

import com.grab.framework.domain.Event;

import java.math.BigDecimal;
import java.time.Instant;

public record CartLineAddedEvent(
        String cartId,
        String variantId,
        int quantity,
        BigDecimal unitPrice,
        Instant occurredAt
) implements Event {
}
