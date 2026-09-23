package com.grab.store.merchant.internal.policy.impl;

import com.grab.store.merchant.internal.policy.MerchantApprovalAccessPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMerchantApprovalAccessPolicyTest {
    private final DefaultMerchantApprovalAccessPolicy policy =
            new DefaultMerchantApprovalAccessPolicy();

    @Test
    void placementsFor_shouldGrantSellerMerchantAdminAccess() {
        var placements = policy.placementsFor(
                new MerchantApprovalAccessPolicy.MerchantApprovalContext("merchant-1")
        );

        assertThat(placements).containsExactly(
                new MerchantApprovalAccessPolicy.AccessPlacement(
                        "SELLER_PORTAL",
                        "MERCHANT_ADMIN",
                        "merchant.account",
                        "merchant-1"
                )
        );
    }
}
