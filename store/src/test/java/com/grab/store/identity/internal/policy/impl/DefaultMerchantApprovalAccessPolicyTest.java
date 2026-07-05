package com.grab.store.identity.internal.policy.impl;

import com.grab.store.identity.internal.policy.MerchantApprovalAccessPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMerchantApprovalAccessPolicyTest {
    private final DefaultMerchantApprovalAccessPolicy policy =
            new DefaultMerchantApprovalAccessPolicy();

    @Test
    void placementsFor_shouldGrantSellerMerchantOwnerAccess() {
        var placements = policy.placementsFor(
                new MerchantApprovalAccessPolicy.MerchantApprovalContext("merchant-1")
        );

        assertThat(placements).containsExactly(
                new MerchantApprovalAccessPolicy.AccessPlacement(
                        "SELLER_PORTAL",
                        "MERCHANT_OWNER",
                        "merchant.account",
                        "merchant-1"
                )
        );
    }
}
