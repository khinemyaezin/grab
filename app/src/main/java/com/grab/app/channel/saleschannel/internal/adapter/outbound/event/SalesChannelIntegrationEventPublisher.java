package com.grab.app.channel.saleschannel.internal.adapter.outbound.event;

import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesChannelIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handleEnabled(SalesChannelEnabledEvent event) {
        events.publishEvent(new SalesChannelEnabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleDisabled(SalesChannelDisabledEvent event) {
        events.publishEvent(new SalesChannelDisabledIntegrationEvent(
                event.salesChannelId(),
                event.type(),
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
