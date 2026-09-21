package com.grab.store.identity.internal.policy.impl;

import com.grab.store.identity.internal.policy.CustomerRegistrationAccessPolicy;
import com.identity.domain.service.CustomerAccessProfile;

import java.util.List;

public final class DefaultCustomerRegistrationAccessPolicy implements CustomerRegistrationAccessPolicy {

    @Override
    public List<AccessPlacement> placementsFor(CustomerRegistrationContext context) {
        return List.of(new AccessPlacement(
                CustomerAccessProfile.CUSTOMER_PLATFORM_CODE,
                CustomerAccessProfile.CUSTOMER_ROLE_CODE,
                CustomerAccessProfile.CUSTOMER_ACCOUNT_SCOPE_KEY,
                context.customerId()
        ));
    }
}
