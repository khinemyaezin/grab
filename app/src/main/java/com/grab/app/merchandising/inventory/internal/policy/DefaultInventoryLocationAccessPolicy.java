package com.grab.app.merchandising.inventory.internal.policy;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.security.InventoryScopeManifest;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import com.merchant.application.security.MerchantScopeManifest;
import org.springframework.stereotype.Component;

@Component
public final class DefaultInventoryLocationAccessPolicy implements InventoryLocationAccessPolicy {

    @Override
    public void requireAccess(String scopeKey, String scopeId, Location location) {
        if (location == null) {
            throw forbidden(scopeKey, scopeId);
        }

        if (InventoryScopeManifest.FULFILLMENT_LOCATION_SCOPE_KEY.equals(scopeKey)) {
            requireFulfillmentLocation(scopeKey, scopeId, location);
            return;
        }

        if (MerchantScopeManifest.ACCOUNT_SCOPE_KEY.equals(scopeKey)) {
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
                        scopeKey == null || scopeKey.isBlank() ? "UNKNOWN" : scopeKey,
                        scopeId == null || scopeId.isBlank() ? "UNKNOWN" : scopeId
                )
        );
    }
}

