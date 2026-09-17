package com.grab.store.identity.internal.event;

import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontStatusChangedIntegrationEvent;
import com.grab.store.shared.security.PlatformScopes;
import com.identity.domain.repository.SessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantSessionRevocationEventListener {
    private final SessionStore sessions;

    @EventListener
    @IdentityTransactional
    public void onMerchantSuspended(MerchantSuspendedIntegrationEvent event) {
        revokeMerchantAccount(event.merchantId());
    }

    @EventListener
    @IdentityTransactional
    public void onMerchantClosed(MerchantClosedIntegrationEvent event) {
        revokeMerchantAccount(event.merchantId());
    }

    @EventListener
    @IdentityTransactional
    public void onStorefrontStatusChanged(StorefrontStatusChangedIntegrationEvent event) {
        if (!"SUSPENDED".equals(event.status()) && !"CLOSED".equals(event.status())) {
            return;
        }
        sessions.revokeByScope(
                PlatformScopes.SELLER_PORTAL,
                PlatformScopes.MERCHANT_STOREFRONT_SCOPE,
                event.storefrontId()
        );
    }

    private void revokeMerchantAccount(String merchantId) {
        sessions.revokeByScope(
                PlatformScopes.SELLER_PORTAL,
                PlatformScopes.MERCHANT_ACCOUNT_SCOPE,
                merchantId
        );
    }
}
