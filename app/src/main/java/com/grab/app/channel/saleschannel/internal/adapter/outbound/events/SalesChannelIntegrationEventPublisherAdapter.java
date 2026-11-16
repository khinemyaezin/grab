package com.grab.app.channel.saleschannel.internal.adapter.outbound.events;

import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.grab.framework.event.DomainEventProducer;
import com.saleschannel.application.port.outbound.SalesChannelIntegrationEventPublisher;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesChannelIntegrationEventPublisherAdapter implements SalesChannelIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final DomainEventProducer outbox;

    public SalesChannelIntegrationEventPublisherAdapter(
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer outbox
    ) {
        this.outbox = outbox;
    }

    @Override
    @EventListener
    public void publishSalesChannelEnabled(SalesChannelEnabledEvent event) {
        var integrationEvent = new SalesChannelEnabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        );
        outbox.produce("SalesChannel", event.salesChannelId(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishSalesChannelDisabled(SalesChannelDisabledEvent event) {
        var integrationEvent = new SalesChannelDisabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        );
        outbox.produce("SalesChannel", event.salesChannelId(), List.of(integrationEvent));
    }
}
