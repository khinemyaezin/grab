package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.app.shared.events.merchant.MerchantClosedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantSuspendedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontStatusChangedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MerchantSessionRevocationEventListener {
    public static final String MERCHANT_ACCOUNT_SCOPE = "merchant";
    public static final String MERCHANT_STOREFRONT_SCOPE = "storefront";

    private final CommandBus commandBus;

    public MerchantSessionRevocationEventListener(@Qualifier("accountCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedIntegrationEvent event) {
        revokeMerchantAccount(event.merchantId());
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedIntegrationEvent event) {
        revokeMerchantAccount(event.merchantId());
    }

    @EventListener
    public void onStorefrontStatusChanged(StorefrontStatusChangedIntegrationEvent event) {
        if (!"SUSPENDED".equals(event.status()) && !"CLOSED".equals(event.status())) {
            return;
        }
        var command = new RevokeSessionsByScopeCommand(
                MERCHANT_STOREFRONT_SCOPE,
                event.storefrontId()
        );
        commandBus.dispatch(command);
    }

    private void revokeMerchantAccount(String merchantId) {
        var command = new RevokeSessionsByScopeCommand(
                MERCHANT_ACCOUNT_SCOPE,
                merchantId
        );
        commandBus.dispatch(command);
    }
}
