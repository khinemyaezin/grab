package com.identity.domain.policy.impl;

import com.identity.domain.aggregate.Platform;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.AccessPlacementPolicy;
import com.identity.domain.service.InventoryAccessProfile;
import com.identity.domain.service.MerchantAccessProfile;
import com.identity.domain.valueobject.AccessScope;

import java.util.Objects;

public final class InventoryOperatorAccessPlacementPolicy implements AccessPlacementPolicy {
    @Override
    public String placementRoleCode() {
        return InventoryAccessProfile.INVENTORY_LOCATION_OPERATOR_ROLE_CODE;
    }

    @Override
    public AccessPlacementPlan plan(
            Platform platform,
            AccessScope targetScope
    ) {
        Objects.requireNonNull(platform, "platform is required");
        Objects.requireNonNull(targetScope, "targetScope is required");

        requireSellerPlatform(platform);
        requireInventoryScope(targetScope);

        String operatorRole = platform.requireSupportedRole(
                InventoryAccessProfile.INVENTORY_LOCATION_OPERATOR_ROLE_CODE
        );

        return new AccessPlacementPlan(
                null,
                AccessScope.global(),
                operatorRole,
                targetScope
        );
    }

    private void requireSellerPlatform(Platform platform) {
        if (!MerchantAccessProfile.SELLER_PLATFORM_CODE.equals(platform.getCode())) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidAccessCode(
                            "platformCode",
                            platform.getCode()
                    ),
                    "Inventory operator placement requires the seller platform"
            );
        }
    }

    private void requireInventoryScope(AccessScope targetScope) {
        if (targetScope.isGlobal()
                || !InventoryAccessProfile.FULFILLMENT_LOCATION_SCOPE_KEY.equals(targetScope.key().value())) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidAccessScope(
                            targetScope.key().value(),
                            targetScope.scopeId()
                    ),
                    "Inventory operator placement requires a fulfillment-location scope"
            );
        }
    }
}
