package com.grab.store.identity.internal.adapter;

import com.grab.store.shared.security.LocationOwnerQuery;
import com.grab.store.shared.security.PlatformScopes;
import com.grab.store.shared.security.StorefrontOwnerQuery;
import com.identity.application.port.outbound.ScopeOwnershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScopeOwnershipPortAdapter implements ScopeOwnershipPort {

    private final StorefrontOwnerQuery storefrontOwnerQuery;
    private final LocationOwnerQuery locationOwnerQuery;

    @Override
    public boolean isResourceOwnedByScope(
            String actorScopeKey,
            String actorScopeId,
            String targetScopeKey,
            String targetScopeId
    ) {
        if (actorScopeKey.equals(targetScopeKey)) {
            return true;
        }
        return switch (actorScopeKey) {
            case PlatformScopes.MERCHANT_ACCOUNT_SCOPE -> ownsMerchantResource(
                    targetScopeKey,
                    actorScopeId,
                    targetScopeId
            );
            default -> false;
        };
    }

    private boolean ownsMerchantResource(String targetScopeKey, String merchantId, String targetId) {
        return switch (targetScopeKey) {
            case PlatformScopes.MERCHANT_STOREFRONT_SCOPE ->
                    storefrontOwnerQuery.belongsToMerchant(targetId, merchantId);
            case PlatformScopes.FULFILLMENT_LOCATION_SCOPE ->
                    locationOwnerQuery.belongsToMerchant(targetId, merchantId);
            default -> false;
        };
    }
}
