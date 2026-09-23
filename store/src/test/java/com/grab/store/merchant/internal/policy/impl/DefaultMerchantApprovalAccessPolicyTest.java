package com.grab.store.merchant.internal.policy.impl;

import com.merchant.domain.policy.impl.DefaultMerchantApprovalAccessPolicy;
import com.merchant.domain.policy.MerchantApprovalAccessPolicy;
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
