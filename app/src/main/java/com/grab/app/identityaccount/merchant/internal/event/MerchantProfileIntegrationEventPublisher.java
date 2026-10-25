package com.grab.app.identityaccount.merchant.internal.event;

import com.grab.app.identityaccount.merchant.internal.config.MerchantEnabled;
import com.grab.app.shared.events.merchant.MerchantApplicationStartedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantProfileUpdatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontRenamedIntegrationEvent;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantProfileIntegrationEventPublisher {
    private final ApplicationEventPublisher events;

    @EventListener
    public void onMerchantApplicationStarted(MerchantApplicationStartedEvent event) {
        String merchantId = event.merchantId();
        String name = event.merchantName();
        long aggregateVersion = event.aggregateVersion();
        Instant occurredAt = event.occurredAt();
        MerchantApplicationStartedIntegrationEvent integrationEvent = new MerchantApplicationStartedIntegrationEvent(
                merchantId, name, aggregateVersion, occurredAt);
        events.publishEvent(integrationEvent);
    }

    @EventListener
    public void onMerchantProfileUpdated(MerchantProfileUpdatedEvent event) {
        String merchantId = event.merchantId();
        String name = event.merchantName();
        long aggregateVersion = event.aggregateVersion();
        Instant occurredAt = event.occurredAt();
        MerchantProfileUpdatedIntegrationEvent integrationEvent = new MerchantProfileUpdatedIntegrationEvent(
                merchantId, name, aggregateVersion, occurredAt);
        events.publishEvent(integrationEvent);
    }

    @EventListener
    public void onStorefrontRenamed(StorefrontRenamedEvent event) {
        String storefrontId = event.storefrontId();
        String merchantId = event.merchantId();
        String name = event.name();
        String slug = event.slug();
        long aggregateVersion = event.aggregateVersion();
        Instant occurredAt = event.occurredAt();
        StorefrontRenamedIntegrationEvent integrationEvent = new StorefrontRenamedIntegrationEvent(
                storefrontId, merchantId, name, slug, aggregateVersion, occurredAt);
        events.publishEvent(integrationEvent);
    }
}
