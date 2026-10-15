package com.grab.store.merchant.internal.event;

import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.events.identity.ScopeDisplayChangedIntegrationEvent;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantScopeDisplayPublicationListener {
    private final ApplicationEventPublisher events;

    @EventListener
    public void onMerchantApplicationStarted(MerchantApplicationStartedEvent event) {
        String scopeId = event.merchantId();
        String name = event.merchantName();
        ScopeDisplayChangedIntegrationEvent integrationEvent = new ScopeDisplayChangedIntegrationEvent(scopeId, name);
        events.publishEvent(integrationEvent);
    }

    @EventListener
    public void onMerchantProfileUpdated(MerchantProfileUpdatedEvent event) {
        String scopeId = event.merchantId();
        String name = event.merchantName();
        ScopeDisplayChangedIntegrationEvent integrationEvent = new ScopeDisplayChangedIntegrationEvent(scopeId, name);
        events.publishEvent(integrationEvent);
    }

    @EventListener
    public void onStorefrontCreated(StorefrontCreatedEvent event) {
        String scopeId = event.storefrontId();
        String name = event.name();
        ScopeDisplayChangedIntegrationEvent integrationEvent = new ScopeDisplayChangedIntegrationEvent(scopeId, name);
        events.publishEvent(integrationEvent);
    }

    @EventListener
    public void onStorefrontRenamed(StorefrontRenamedEvent event) {
        String scopeId = event.storefrontId();
        String name = event.name();
        ScopeDisplayChangedIntegrationEvent integrationEvent = new ScopeDisplayChangedIntegrationEvent(scopeId, name);
        events.publishEvent(integrationEvent);
    }
}
