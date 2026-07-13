package com.grab.store.inventory.internal.policy.impl;

import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import com.grab.store.shared.security.PlatformScopes;
import com.inventory.domain.aggregate.Location;

public final class DefaultInventoryLocationAccessPolicy implements InventoryLocationAccessPolicy {

    @Override
    public void requireAccess(String scopeKey, String scopeId, Location location) {
        if (location == null) {
            throw forbidden(scopeKey, scopeId);
        }

        if (PlatformScopes.FULFILLMENT_LOCATION_SCOPE.equals(scopeKey)) {
            requireFulfillmentLocation(scopeKey, scopeId, location);
            return;
        }

        if (PlatformScopes.MERCHANT_ACCOUNT_SCOPE.equals(scopeKey)) {
            requireMerchantAccount(scopeKey, scopeId, location);
            return;
        }

        throw forbidden(scopeKey, scopeId);
    }

    private void requireFulfillmentLocation(String scopeKey, String scopeId, Location location) {
        if (scopeId == null || scopeId.isBlank()
                || !location.getId().getValue().equals(scopeId)) {
            throw forbidden(scopeKey, scopeId);
        }
    }

    private void requireMerchantAccount(String scopeKey, String scopeId, Location location) {
        if (scopeId == null || scopeId.isBlank()
                || !location.getMerchantId().getValue().equals(scopeId)) {
            throw forbidden(scopeKey, scopeId);
        }
    }

    private InventoryServiceException forbidden(String scopeKey, String scopeId) {
        return new InventoryServiceException(
                new InventoryServiceError.InventoryScopeForbidden(
                        PlatformScopes.SELLER_PORTAL,
                        scopeKey == null || scopeKey.isBlank() ? "UNKNOWN" : scopeKey,
                        scopeId == null || scopeId.isBlank() ? "UNKNOWN" : scopeId
                )
        );
    }
}
