package com.grab.store.merchant.internal.policy.impl;

import com.grab.store.merchant.internal.policy.MerchantApprovalAccessPolicy;
import com.merchant.domain.event.MerchantAccessProfile;

import java.util.List;

public final class DefaultMerchantApprovalAccessPolicy implements MerchantApprovalAccessPolicy {

    @Override
    public List<AccessPlacement> placementsFor(MerchantApprovalContext context) {
        return List.of(new AccessPlacement(
                MerchantAccessProfile.ADMIN_ROLE_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                context.merchantId()
        ));
    }
}
