package com.pricing.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PriceSetUpdatedEvent(String priceSetId, Instant occurredAt) implements Event {
}
