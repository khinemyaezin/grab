package com.grab.store.identity.internal.policy;

import java.util.List;

public interface MerchantApprovalAccessPolicy {
    List<AccessPlacement> placementsFor(MerchantApprovalContext context);

    record MerchantApprovalContext(String merchantId) {
    }

    record AccessPlacement(
            String platformCode,
            String placementCode,
            String scopeKey,
            String scopeId
    ) {
    }
}
