package com.grab.store.pricing.internal.event;

import com.grab.store.pricing.events.PriceSetChangedIntegrationEvent;
import com.pricing.domain.event.PriceSetCreatedEvent;
import com.pricing.domain.event.PriceSetUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PricingIntegrationEventPublisherTest {

    private final AtomicReference<Object> published = new AtomicReference<>();
    private final ApplicationEventPublisher events = published::set;
    private final PricingIntegrationEventPublisher publisher = new PricingIntegrationEventPublisher(events);

    @Test
    void handlePriceSetCreated_shouldPublishChangedEvent() {
        publisher.handlePriceSetCreated(new PriceSetCreatedEvent("price-set-1", Instant.now()));

        assertThat(published.get()).isInstanceOfSatisfying(PriceSetChangedIntegrationEvent.class, event ->
                assertThat(event.priceSetId()).isEqualTo("price-set-1"));
    }

    @Test
    void handlePriceSetUpdated_shouldPublishChangedEvent() {
        publisher.handlePriceSetUpdated(new PriceSetUpdatedEvent("price-set-2", Instant.now()));

        assertThat(published.get()).isInstanceOfSatisfying(PriceSetChangedIntegrationEvent.class, event ->
                assertThat(event.priceSetId()).isEqualTo("price-set-2"));
    }
}
