package com.grab.store.merchant.internal.event;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantReactivatedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontCreatedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontStatusChangedIntegrationEvent;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantLifecycleEvent;
import com.merchant.domain.event.MerchantReactivatedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;
    private final MerchantAccountRepository merchants;
    private final IdGenerator ids;

    @EventListener
    public void handleMerchantApproved(MerchantApprovedEvent event) {
        events.publishEvent(new MerchantApprovedIntegrationEvent(
                event.merchantId(),
                event.applicantUserId(),
                event.merchantName(),
                merchantType(event),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleMerchantSuspended(MerchantSuspendedEvent event) {
        events.publishEvent(new MerchantSuspendedIntegrationEvent(
                event.merchantId(),
                event.applicantUserId(),
                event.merchantName(),
                merchantType(event),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleMerchantReactivated(MerchantReactivatedEvent event) {
        events.publishEvent(new MerchantReactivatedIntegrationEvent(
                event.merchantId(),
                event.applicantUserId(),
                event.merchantName(),
                merchantType(event),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleMerchantClosed(MerchantClosedEvent event) {
        events.publishEvent(new MerchantClosedIntegrationEvent(
                event.merchantId(),
                event.applicantUserId(),
                event.merchantName(),
                merchantType(event),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleStorefrontCreated(StorefrontCreatedEvent event) {
        events.publishEvent(new StorefrontCreatedIntegrationEvent(
                event.storefrontId(),
                event.merchantId(),
                event.name(),
                event.slug(),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleStorefrontStatusChanged(StorefrontStatusChangedEvent event) {
        events.publishEvent(new StorefrontStatusChangedIntegrationEvent(
                event.storefrontId(),
                event.merchantId(),
                event.slug(),
                event.previousStatus(),
                event.status(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    private String merchantType(MerchantLifecycleEvent event) {
        Id merchantId = ids.convertIdFrom(event.merchantId());
        return merchants.findById(merchantId)
                .map(MerchantAccount::getType)
                .map(Enum::name)
                .orElse(null);
    }
}
