package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontStatusChangedIntegrationEvent;
import com.grab.store.shared.security.PlatformScopes;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantSessionRevocationEventListener {
    private final CommandBus commandBus;

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
                PlatformScopes.SELLER_PORTAL,
                PlatformScopes.MERCHANT_STOREFRONT_SCOPE,
                event.storefrontId()
        );
        commandBus.dispatch(command);
    }

    private void revokeMerchantAccount(String merchantId) {
        var command = new RevokeSessionsByScopeCommand(
                PlatformScopes.SELLER_PORTAL,
                PlatformScopes.MERCHANT_ACCOUNT_SCOPE,
                merchantId
        );
        commandBus.dispatch(command);
    }
}
