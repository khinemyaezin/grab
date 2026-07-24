package com.pricing.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PriceListCreatedEvent(String priceListId, Instant occurredAt) implements Event {
}
