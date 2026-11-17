package com.grab.service.account.merchant.internal.adapter.outbound.event;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.service.contract.event.account.merchant.MerchantClosedIntegrationEvent;
import com.grab.service.contract.event.account.merchant.MerchantReactivatedIntegrationEvent;
import com.grab.service.contract.event.account.merchant.MerchantSuspendedIntegrationEvent;
import com.grab.service.contract.event.account.merchant.StorefrontStatusChangedIntegrationEvent;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantLifecycleEvent;
import com.merchant.domain.event.MerchantReactivatedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantLifecycleEventPublisher {
    private final DomainEventProducer outbox;
    private final QueryBus queries;
    private final IdGenerator ids;

    public MerchantLifecycleEventPublisher(@Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
                                           @Qualifier("accountQueryBus") QueryBus queries, IdGenerator ids) {
        this.outbox = outbox;
        this.queries = queries;
        this.ids = ids;
    }

    @EventListener
    public void suspended(MerchantSuspendedEvent event) {
        String type = merchantType(event);
        MerchantSuspendedIntegrationEvent integration = new MerchantSuspendedIntegrationEvent(
                event.merchantId(), event.applicantUserId(), event.merchantName(), type, event.status(),
                event.occurredAt(), 1);
        outbox.produce("MerchantAccount", event.merchantId(), List.of(integration));
    }

    @EventListener
    public void closed(MerchantClosedEvent event) {
        String type = merchantType(event);
        MerchantClosedIntegrationEvent integration = new MerchantClosedIntegrationEvent(
                event.merchantId(), event.applicantUserId(), event.merchantName(), type, event.status(),
                event.occurredAt(), 1);
        outbox.produce("MerchantAccount", event.merchantId(), List.of(integration));
    }

    @EventListener
    public void reactivated(MerchantReactivatedEvent event) {
        String type = merchantType(event);
        MerchantReactivatedIntegrationEvent integration = new MerchantReactivatedIntegrationEvent(
                event.merchantId(), event.applicantUserId(), event.merchantName(), type, event.status(),
                event.occurredAt(), 1);
        outbox.produce("MerchantAccount", event.merchantId(), List.of(integration));
    }

    @EventListener
    public void storefrontStatus(StorefrontStatusChangedEvent event) {
        StorefrontStatusChangedIntegrationEvent integration = new StorefrontStatusChangedIntegrationEvent(
                event.storefrontId(), event.merchantId(), event.slug(), event.previousStatus(), event.status(),
                event.occurredAt(), 1);
        outbox.produce("Storefront", event.storefrontId(), List.of(integration));
    }

    private String merchantType(MerchantLifecycleEvent event) {
        Id merchant = ids.convertIdFrom(event.merchantId());
        Id actor = ids.convertIdFrom(event.applicantUserId());
        GetMerchantQuery query = new GetMerchantQuery(merchant, actor, true, false);
        MerchantAccountResult result = queries.dispatch(query);
        return result == null ? null : result.type();
    }
}
