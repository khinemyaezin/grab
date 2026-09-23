package com.grab.store.identity.internal.policy;

import java.util.List;

public interface CustomerRegistrationAccessPolicy {
    List<AccessPlacement> placementsFor(CustomerRegistrationContext context);

    record CustomerRegistrationContext(String customerId) {
    }

    record AccessPlacement(
            String platformCode,
            String placementCode,
            String scopeKey,
            String scopeId
    ) {
    }
}
