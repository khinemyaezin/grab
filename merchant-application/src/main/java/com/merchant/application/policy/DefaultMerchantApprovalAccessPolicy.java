package com.merchant.application.policy;

import com.merchant.application.util.MerchantAccessProfile;
import com.merchant.domain.policy.MerchantApprovalAccessPolicy;

import java.util.List;

public final class DefaultMerchantApprovalAccessPolicy
        implements MerchantApprovalAccessPolicy {

    @Override
    public List<AccessPlacement> placementsFor(MerchantApprovalContext context) {
        return List.of(new AccessPlacement(
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.OWNER_ROLE_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                context.merchantId()
        ));
    }
}
