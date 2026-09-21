package com.grab.store.pricing.internal.event;

import com.grab.store.pricing.events.PriceSetChangedIntegrationEvent;
import com.pricing.domain.event.PriceSetCreatedEvent;
import com.pricing.domain.event.PriceSetUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PricingIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handlePriceSetCreated(PriceSetCreatedEvent event) {
        events.publishEvent(new PriceSetChangedIntegrationEvent(
                event.priceSetId(),
                event.occurredAt() == null ? Instant.now() : event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handlePriceSetUpdated(PriceSetUpdatedEvent event) {
        events.publishEvent(new PriceSetChangedIntegrationEvent(
                event.priceSetId(),
                event.occurredAt() == null ? Instant.now() : event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
