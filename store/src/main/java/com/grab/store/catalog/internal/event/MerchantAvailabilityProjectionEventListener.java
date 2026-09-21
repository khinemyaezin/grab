package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantReactivatedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantAvailabilityProjectionEventListener {
    private final CommandBus commandBus;

    @EventListener
    public void onMerchantApproved(MerchantApprovedIntegrationEvent event) {
        upsert(event.merchantId(), event.status(), event.merchantType());
    }

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedIntegrationEvent event) {
        upsert(event.merchantId(), event.status(), event.merchantType());
    }

    @EventListener
    public void onMerchantReactivated(MerchantReactivatedIntegrationEvent event) {
        upsert(event.merchantId(), event.status(), event.merchantType());
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedIntegrationEvent event) {
        upsert(event.merchantId(), event.status(), event.merchantType());
    }

    private void upsert(String merchantId, String status, String merchantType) {
        commandBus.dispatch(new UpsertMerchantAvailabilityCommand(merchantId, status, merchantType));
    }
}
