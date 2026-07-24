package com.pricing.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PriceSetCreatedEvent(String priceSetId, Instant occurredAt) implements Event {
}
