package com.grab.store.merchant.internal.policy.impl;

import com.grab.store.merchant.internal.policy.MerchantApprovalAccessPolicy;
import com.merchant.application.security.MerchantAdminAccessProfile;

import java.util.List;

public final class DefaultMerchantApprovalAccessPolicy implements MerchantApprovalAccessPolicy {

    @Override
    public List<AccessPlacement> placementsFor(MerchantApprovalContext context) {
        return List.of(new AccessPlacement(
                MerchantAdminAccessProfile.ADMIN_ROLE_CODE,
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                context.merchantId()
        ));
    }
}
