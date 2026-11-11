package com.grab.app.account.merchant.internal.adapter.outbound.events;

import com.grab.app.shared.events.merchant.MerchantApplicationStartedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantClosedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantProfileUpdatedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantReactivatedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantSuspendedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontCreatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontRenamedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontStatusChangedIntegrationEvent;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.MerchantReactivatedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantIntegrationEventPublisherAdapterTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-10-01T12:00:00Z");

    @Mock
    private ApplicationEventPublisher events;

    @Mock
    private MerchantAccountRepository merchants;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private final IdGenerator ids = new UuidGenerator();
    private MerchantIntegrationEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MerchantIntegrationEventPublisherAdapter(events, merchants, ids);
    }

    @Test
    void publishMerchantApproved_publishesIntegrationEvent() {
        MerchantApprovedEvent domainEvent = new MerchantApprovedEvent(
                "merchant-1", "Shop 1", "user-1", "ACTIVE", "reviewer-1", 1, OCCURRED_AT, "SG"
        );

        adapter.publishMerchantApproved(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantApprovedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.primaryMarketCountryCode()).isEqualTo("SG");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishMerchantSuspended_resolvesMerchantTypeAndPublishes() {
        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchant.getType()).thenReturn(MerchantType.FIRST_PARTY_RETAILER);
        when(merchants.findById(any(Id.class))).thenReturn(Optional.of(merchant));

        MerchantSuspendedEvent domainEvent = new MerchantSuspendedEvent(
                "merchant-1", "Shop 1", "user-1", "SUSPENDED", "admin", 2, OCCURRED_AT
        );

        adapter.publishMerchantSuspended(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantSuspendedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.merchantType()).isEqualTo("FIRST_PARTY_RETAILER");
            assertThat(event.status()).isEqualTo("SUSPENDED");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishMerchantReactivated_resolvesMerchantTypeAndPublishes() {
        MerchantAccount merchant = mock(MerchantAccount.class);
        when(merchant.getType()).thenReturn(MerchantType.THIRD_PARTY);
        when(merchants.findById(any(Id.class))).thenReturn(Optional.of(merchant));

        MerchantReactivatedEvent domainEvent = new MerchantReactivatedEvent(
                "merchant-1", "Shop 1", "user-1", "ACTIVE", "admin", 3, OCCURRED_AT
        );

        adapter.publishMerchantReactivated(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantReactivatedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.merchantType()).isEqualTo("THIRD_PARTY");
            assertThat(event.status()).isEqualTo("ACTIVE");
        });
    }

    @Test
    void publishMerchantClosed_resolvesMerchantTypeAndPublishes() {
        when(merchants.findById(any(Id.class))).thenReturn(Optional.empty());

        MerchantClosedEvent domainEvent = new MerchantClosedEvent(
                "merchant-1", "Shop 1", "user-1", "CLOSED", "admin", 4, OCCURRED_AT
        );

        adapter.publishMerchantClosed(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantClosedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.merchantType()).isNull();
            assertThat(event.status()).isEqualTo("CLOSED");
        });
    }

    @Test
    void publishStorefrontCreated_publishesIntegrationEvent() {
        StorefrontCreatedEvent domainEvent = new StorefrontCreatedEvent(
                "storefront-1", "merchant-1", "Shop Front", "shop-front", "ACTIVE", 1L, OCCURRED_AT
        );

        adapter.publishStorefrontCreated(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StorefrontCreatedIntegrationEvent.class, event -> {
            assertThat(event.storefrontId()).isEqualTo("storefront-1");
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.name()).isEqualTo("Shop Front");
            assertThat(event.slug()).isEqualTo("shop-front");
            assertThat(event.status()).isEqualTo("ACTIVE");
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishStorefrontStatusChanged_publishesIntegrationEvent() {
        StorefrontStatusChangedEvent domainEvent = new StorefrontStatusChangedEvent(
                "storefront-1", "merchant-1", "shop-front", "ACTIVE", "SUSPENDED", 2L, OCCURRED_AT
        );

        adapter.publishStorefrontStatusChanged(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StorefrontStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.storefrontId()).isEqualTo("storefront-1");
            assertThat(event.previousStatus()).isEqualTo("ACTIVE");
            assertThat(event.status()).isEqualTo("SUSPENDED");
        });
    }

    @Test
    void publishMerchantApplicationStarted_publishesIntegrationEvent() {
        MerchantApplicationStartedEvent domainEvent = new MerchantApplicationStartedEvent(
                "merchant-1", "New Shop", "user-1", "DRAFT", "user-1", 1L, OCCURRED_AT
        );

        adapter.publishMerchantApplicationStarted(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantApplicationStartedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.merchantName()).isEqualTo("New Shop");
            assertThat(event.aggregateVersion()).isEqualTo(1L);
        });
    }

    @Test
    void publishMerchantProfileUpdated_publishesIntegrationEvent() {
        MerchantProfileUpdatedEvent domainEvent = new MerchantProfileUpdatedEvent(
                "merchant-1", "Updated Shop", 2L, OCCURRED_AT
        );

        adapter.publishMerchantProfileUpdated(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(MerchantProfileUpdatedIntegrationEvent.class, event -> {
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.merchantName()).isEqualTo("Updated Shop");
            assertThat(event.aggregateVersion()).isEqualTo(2L);
        });
    }

    @Test
    void publishStorefrontRenamed_publishesIntegrationEvent() {
        StorefrontRenamedEvent domainEvent = new StorefrontRenamedEvent(
                "storefront-1", "merchant-1", "Old Name", "New Name", "old-slug", "new-slug", 3L, OCCURRED_AT
        );

        adapter.publishStorefrontRenamed(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StorefrontRenamedIntegrationEvent.class, event -> {
            assertThat(event.storefrontId()).isEqualTo("storefront-1");
            assertThat(event.name()).isEqualTo("New Name");
            assertThat(event.slug()).isEqualTo("new-slug");
        });
    }
}
