package com.grab.store.inventory.internal.policy.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.shared.security.PlatformScopes;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultInventoryLocationAccessPolicyTest {

    private DefaultInventoryLocationAccessPolicy policy;

    private final Id locationId = new CommonId("loc-1");
    private final Id merchantId = new CommonId("merchant-1");
    private Location location;

    @BeforeEach
    void setUp() {
        policy = new DefaultInventoryLocationAccessPolicy();
        location = Location.create(locationId, merchantId, "WH-1", "Warehouse", LocationType.WAREHOUSE, null);
    }

    @Test
    void requireAccess_withMatchingFulfillmentLocation_shouldPass() {
        assertThatCode(() -> policy.requireAccess(
                PlatformScopes.FULFILLMENT_LOCATION_SCOPE, "loc-1", location))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAccess_withMismatchedFulfillmentLocation_shouldThrow() {
        assertThatThrownBy(() -> policy.requireAccess(
                PlatformScopes.FULFILLMENT_LOCATION_SCOPE, "loc-other", location))
                .isInstanceOf(InventoryServiceException.class);
    }

    @Test
    void requireAccess_withMatchingMerchantAccount_shouldPass() {
        assertThatCode(() -> policy.requireAccess(
                PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "merchant-1", location))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAccess_withMismatchedMerchantAccount_shouldThrow() {
        assertThatThrownBy(() -> policy.requireAccess(
                PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "merchant-other", location))
                .isInstanceOf(InventoryServiceException.class);
    }

    @Test
    void requireAccess_withNullLocation_shouldThrow() {
        assertThatThrownBy(() -> policy.requireAccess(
                PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "merchant-1", null))
                .isInstanceOf(InventoryServiceException.class);
    }

    @Test
    void requireAccess_withUnknownScope_shouldThrow() {
        assertThatThrownBy(() -> policy.requireAccess(
                "other.scope", "merchant-1", location))
                .isInstanceOf(InventoryServiceException.class);
    }
}
