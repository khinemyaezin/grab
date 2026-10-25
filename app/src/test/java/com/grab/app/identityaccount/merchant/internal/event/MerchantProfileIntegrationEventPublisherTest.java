package com.grab.app.identityaccount.merchant.internal.event;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.app.shared.events.merchant.MerchantApplicationStartedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantProfileUpdatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontCreatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontRenamedIntegrationEvent;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class MerchantProfileIntegrationEventPublisherTest {
    private static final Instant OCCURRED_AT = Instant.parse("2026-10-11T10:00:00Z");

    private final List<Object> published = new ArrayList<>();
    private final ApplicationEventPublisher events = published::add;
    private final MerchantProfileIntegrationEventPublisher publisher =
            new MerchantProfileIntegrationEventPublisher(events);

    @Test
    void onMerchantApplicationStarted_domainEvent_publishesBusinessSnapshot() {
        MerchantApplicationStartedEvent event = new MerchantApplicationStartedEvent(
                "merchant-1", "Shop 1", "user-1", "DRAFT", "actor-1", 1L, OCCURRED_AT);
        MerchantApplicationStartedIntegrationEvent expected = new MerchantApplicationStartedIntegrationEvent(
                "merchant-1", "Shop 1", 1L, OCCURRED_AT);

        publisher.onMerchantApplicationStarted(event);

        assertThat(published).containsExactly(expected);
    }

    @Test
    void onMerchantProfileUpdated_domainEvent_preservesNameRevisionAndTimestamp() {
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-2", "Updated Shop", 2L, OCCURRED_AT);
        MerchantProfileUpdatedIntegrationEvent expected = new MerchantProfileUpdatedIntegrationEvent(
                "merchant-2", "Updated Shop", 2L, OCCURRED_AT);

        publisher.onMerchantProfileUpdated(event);

        assertThat(published).containsExactly(expected);
    }

    @Test
    void onStorefrontRenamed_domainEvent_preservesCurrentBusinessSnapshot() {
        StorefrontRenamedEvent event = new StorefrontRenamedEvent(
                "storefront-1", "merchant-1", "Old Store", "Renamed Store",
                "old-slug", "renamed-slug", 3L, OCCURRED_AT);
        StorefrontRenamedIntegrationEvent expected = new StorefrontRenamedIntegrationEvent(
                "storefront-1", "merchant-1", "Renamed Store", "renamed-slug", 3L, OCCURRED_AT);

        publisher.onStorefrontRenamed(event);

        assertThat(published).containsExactly(expected);
    }

    @Test
    void handleStorefrontCreated_domainEvent_publishesExistingBusinessSnapshotWithoutLookup() {
        MerchantAccountRepository merchants = mock(MerchantAccountRepository.class);
        IdGenerator ids = new UuidGenerator();
        MerchantIntegrationEventPublisher lifecyclePublisher = new MerchantIntegrationEventPublisher(
                events, merchants, ids);
        StorefrontCreatedEvent event = new StorefrontCreatedEvent(
                "storefront-1", "merchant-1", "Store 1", "store-1", "DRAFT", 1L, OCCURRED_AT);
        StorefrontCreatedIntegrationEvent expected = new StorefrontCreatedIntegrationEvent(
                "storefront-1", "merchant-1", "Store 1", "store-1", "DRAFT", OCCURRED_AT, 1);

        lifecyclePublisher.handleStorefrontCreated(event);

        assertThat(published).containsExactly(expected);
        verifyNoInteractions(merchants);
    }
}
