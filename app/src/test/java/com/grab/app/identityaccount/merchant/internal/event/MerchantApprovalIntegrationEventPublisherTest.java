package com.grab.app.identityaccount.merchant.internal.event;

import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantApprovalIntegrationEventPublisherTest {
    private final AtomicReference<Object> published = new AtomicReference<>();
    private final MerchantApprovalIntegrationEventPublisher publisher =
            new MerchantApprovalIntegrationEventPublisher(published::set);

    @Test
    void onMerchantApproved_publishesIntegrationEvent() {
        Instant occurredAt = Instant.parse("2026-10-01T00:00:00Z");

        publisher.onMerchantApproved(new MerchantApprovedEvent(
                "merchant-1", "Merchant", "user-1", "ACTIVE", "reviewer-1", 2, occurredAt, "US"
        ));

        assertThat(published.get()).isEqualTo(new MerchantApprovedIntegrationEvent("merchant-1", "US", occurredAt, 1));
    }
}
