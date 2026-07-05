package com.grab.store.merchant.internal.event;

import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantIntegrationEventPublisherTest {
    @Test
    void handleMerchantApproved_shouldPublishPublicIntegrationEvent() {
        AtomicReference<Object> published = new AtomicReference<>();
        ApplicationEventPublisher events = published::set;
        MerchantIntegrationEventPublisher publisher = new MerchantIntegrationEventPublisher(events);
        Instant occurredAt = Instant.parse("2026-07-02T00:00:00Z");

        publisher.handleMerchantApproved(new MerchantApprovedEvent(
                "event-1",
                "merchant-1",
                "applicant-1",
                "ACTIVE",
                "reviewer-1",
                2,
                occurredAt
        ));

        assertThat(published.get()).isEqualTo(new MerchantApprovedIntegrationEvent(
                "event-1",
                "merchant-1",
                "applicant-1",
                occurredAt,
                1
        ));
    }
}
