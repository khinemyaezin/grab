package com.grab.store.merchant.internal.event;

import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantApprovalIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void onMerchantApproved(MerchantApprovedEvent event) {
        MerchantApprovedIntegrationEvent integrationEvent = new MerchantApprovedIntegrationEvent(
                event.merchantId(),
                event.occurredAt(),
                EVENT_VERSION
        );
        events.publishEvent(integrationEvent);
    }
}
