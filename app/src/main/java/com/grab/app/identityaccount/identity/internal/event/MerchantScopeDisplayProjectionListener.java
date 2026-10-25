package com.grab.app.identityaccount.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.app.shared.events.merchant.MerchantApplicationStartedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantProfileUpdatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontCreatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontRenamedIntegrationEvent;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantScopeDisplayProjectionListener {
    private final CommandBus commandBus;
    private final IdGenerator ids;

    @EventListener
    public void onMerchantApplicationStarted(MerchantApplicationStartedIntegrationEvent event) {
        String merchantId = event.merchantId();
        String name = event.merchantName();
        updateDisplay(merchantId, name);
    }

    @EventListener
    public void onMerchantProfileUpdated(MerchantProfileUpdatedIntegrationEvent event) {
        String merchantId = event.merchantId();
        String name = event.merchantName();
        updateDisplay(merchantId, name);
    }

    @EventListener
    public void onStorefrontCreated(StorefrontCreatedIntegrationEvent event) {
        String storefrontId = event.storefrontId();
        String name = event.name();
        updateDisplay(storefrontId, name);
    }

    @EventListener
    public void onStorefrontRenamed(StorefrontRenamedIntegrationEvent event) {
        String storefrontId = event.storefrontId();
        String name = event.name();
        updateDisplay(storefrontId, name);
    }

    private void updateDisplay(String scopeIdValue, String name) {
        Id scopeId = ids.convertIdFrom(scopeIdValue);
        UpdateScopeDisplayCommand command = new UpdateScopeDisplayCommand(scopeId, name);
        commandBus.dispatch(command);
    }
}
