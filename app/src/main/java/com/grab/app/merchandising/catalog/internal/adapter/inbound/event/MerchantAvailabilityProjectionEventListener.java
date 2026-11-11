package com.grab.app.merchandising.catalog.internal.adapter.inbound.event;

import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;
import com.grab.app.shared.events.merchant.MerchantClosedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantReactivatedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantSuspendedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MerchantAvailabilityProjectionEventListener {
    private final CommandBus commandBus;

    public MerchantAvailabilityProjectionEventListener(@Qualifier("merchandisingCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
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
