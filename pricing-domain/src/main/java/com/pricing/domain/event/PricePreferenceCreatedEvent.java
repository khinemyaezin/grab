package com.pricing.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PricePreferenceCreatedEvent(String pricePreferenceId, Instant occurredAt) implements Event {
}
