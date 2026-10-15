package com.grab.store.merchant.internal.event;

import com.grab.store.shared.events.identity.ScopeDisplayChangedIntegrationEvent;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantScopeDisplayPublicationListenerTest {
    private final List<Object> published = new ArrayList<>();
    private final ApplicationEventPublisher events = published::add;
    private final MerchantScopeDisplayPublicationListener listener =
            new MerchantScopeDisplayPublicationListener(events);

    @Test
    void onMerchantApplicationStarted_publishesIntegrationEvent() {
        MerchantApplicationStartedEvent event = new MerchantApplicationStartedEvent(
                "merchant-1", "Shop 1", "user-1", "DRAFT", "actor-1", 1L, Instant.now());

        listener.onMerchantApplicationStarted(event);

        assertThat(published).containsExactly(new ScopeDisplayChangedIntegrationEvent("merchant-1", "Shop 1"));
    }

    @Test
    void onMerchantProfileUpdated_publishesIntegrationEvent() {
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-2", "Updated Shop", 2L, Instant.now());

        listener.onMerchantProfileUpdated(event);

        assertThat(published).containsExactly(new ScopeDisplayChangedIntegrationEvent("merchant-2", "Updated Shop"));
    }

    @Test
    void onStorefrontCreated_publishesIntegrationEvent() {
        StorefrontCreatedEvent event = new StorefrontCreatedEvent(
                "storefront-1", "merchant-1", "Store 1", "store-1", "ACTIVE", 1L, Instant.now());

        listener.onStorefrontCreated(event);

        assertThat(published).containsExactly(new ScopeDisplayChangedIntegrationEvent("storefront-1", "Store 1"));
    }

    @Test
    void onStorefrontRenamed_publishesIntegrationEvent() {
        StorefrontRenamedEvent event = new StorefrontRenamedEvent(
                "storefront-1", "merchant-1", "Old Store", "Renamed Store",
                "old-slug", "renamed-slug", 3L, Instant.now());

        listener.onStorefrontRenamed(event);

        assertThat(published).containsExactly(new ScopeDisplayChangedIntegrationEvent("storefront-1", "Renamed Store"));
    }
}
