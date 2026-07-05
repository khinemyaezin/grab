package com.identity.domain.policy;

import com.identity.domain.aggregate.Platform;
import com.identity.domain.valueobject.AccessScope;

public interface AccessPlacementPolicy {
    String placementRoleCode();

    AccessPlacementPlan plan(Platform platform, AccessScope accessScope);

    record AccessPlacementPlan(
            String previousRoleCode,
            AccessScope previousScope,
            String replacementRoleCode,
            AccessScope replacementScope
    ) {
    }
}
