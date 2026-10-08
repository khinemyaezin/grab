package com.identity.domain.policy;

import com.identity.domain.valueobject.AccessScope;

public interface AccessPlacementPolicy {
    String placementRoleCode();

    AccessPlacementPlan plan(AccessScope accessScope);

    record AccessPlacementPlan(
            String previousRoleCode,
            AccessScope previousScope,
            String replacementRoleCode,
            AccessScope replacementScope
    ) {
    }
}
