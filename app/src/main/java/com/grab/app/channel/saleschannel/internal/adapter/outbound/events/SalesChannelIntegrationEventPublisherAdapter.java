package com.grab.app.channel.saleschannel.internal.adapter.outbound.events;

import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.saleschannel.application.port.outbound.SalesChannelIntegrationEventPublisher;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SalesChannelIntegrationEventPublisherAdapter implements SalesChannelIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    public SalesChannelIntegrationEventPublisherAdapter(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    @EventListener
    public void publishSalesChannelEnabled(SalesChannelEnabledEvent event) {
        events.publishEvent(new SalesChannelEnabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishSalesChannelDisabled(SalesChannelDisabledEvent event) {
        events.publishEvent(new SalesChannelDisabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
