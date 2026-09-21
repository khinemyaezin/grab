package com.identity.domain.policy.impl;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.policy.RegistrationAccessPolicy;
import com.identity.domain.service.CustomerAccessProfile;
import com.identity.domain.valueobject.AccessScope;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CustomerAppUserRegistrationAccessPolicy implements RegistrationAccessPolicy {

    @Override
    public String platformCode() {
        return CustomerAccessProfile.CUSTOMER_PLATFORM_CODE;
    }

    @Override
    public AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform) {
        platform.requireSupportedRole(CustomerAccessProfile.CUSTOMER_ROLE_CODE);

        return AccessAssignment.create(
                assignmentId,
                userId,
                platform,
                CustomerAccessProfile.CUSTOMER_ROLE_CODE,
                AccessScope.global(),
                null,
                null
        );
    }
}
