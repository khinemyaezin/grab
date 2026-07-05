package com.grab.store.merchant.internal.event;

import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handleMerchantApproved(MerchantApprovedEvent event) {
        events.publishEvent(new MerchantApprovedIntegrationEvent(
                event.eventId(),
                event.merchantId(),
                event.applicantUserId(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
