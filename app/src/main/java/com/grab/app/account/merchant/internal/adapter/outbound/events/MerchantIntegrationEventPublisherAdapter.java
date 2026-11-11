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
import com.merchant.application.port.outbound.MerchantIntegrationEventPublisher;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantLifecycleEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.MerchantReactivatedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MerchantIntegrationEventPublisherAdapter implements MerchantIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;
    private final MerchantAccountRepository merchants;
    private final IdGenerator ids;

    public MerchantIntegrationEventPublisherAdapter(
            ApplicationEventPublisher events,
            MerchantAccountRepository merchants,
            IdGenerator ids
    ) {
        this.events = events;
        this.merchants = merchants;
        this.ids = ids;
    }

    @Override
    @EventListener
    public void publishMerchantApproved(MerchantApprovedEvent event) {
        events.publishEvent(new MerchantApprovedIntegrationEvent(
                event.merchantId(),
                event.primaryMarketCountryCode(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishMerchantSuspended(MerchantSuspendedEvent event) {
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

    @Override
    @EventListener
    public void publishMerchantReactivated(MerchantReactivatedEvent event) {
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

    @Override
    @EventListener
    public void publishMerchantClosed(MerchantClosedEvent event) {
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

    @Override
    @EventListener
    public void publishStorefrontCreated(StorefrontCreatedEvent event) {
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

    @Override
    @EventListener
    public void publishStorefrontStatusChanged(StorefrontStatusChangedEvent event) {
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

    @Override
    @EventListener
    public void publishMerchantApplicationStarted(MerchantApplicationStartedEvent event) {
        events.publishEvent(new MerchantApplicationStartedIntegrationEvent(
                event.merchantId(),
                event.merchantName(),
                event.aggregateVersion(),
                event.occurredAt()
        ));
    }

    @Override
    @EventListener
    public void publishMerchantProfileUpdated(MerchantProfileUpdatedEvent event) {
        events.publishEvent(new MerchantProfileUpdatedIntegrationEvent(
                event.merchantId(),
                event.merchantName(),
                event.aggregateVersion(),
                event.occurredAt()
        ));
    }

    @Override
    @EventListener
    public void publishStorefrontRenamed(StorefrontRenamedEvent event) {
        events.publishEvent(new StorefrontRenamedIntegrationEvent(
                event.storefrontId(),
                event.merchantId(),
                event.name(),
                event.slug(),
                event.aggregateVersion(),
                event.occurredAt()
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
