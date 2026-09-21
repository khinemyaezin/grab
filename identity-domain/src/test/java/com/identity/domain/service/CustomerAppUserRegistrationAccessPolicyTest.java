package com.identity.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.policy.impl.CustomerAppUserRegistrationAccessPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerAppUserRegistrationAccessPolicyTest {

    @Test
    void createAssignment_shouldGrantGlobalCustomerRoleOnCustomerApp() {
        var policy = new CustomerAppUserRegistrationAccessPolicy();
        var platform = new Platform(
                new CommonId("platform-1"),
                CustomerAccessProfile.CUSTOMER_PLATFORM_CODE,
                "Customer App",
                true,
                Set.of(CustomerAccessProfile.CUSTOMER_ROLE_CODE)
        );

        var assignment = policy.createAssignment(
                new CommonId("assignment-1"),
                new CommonId("user-1"),
                platform
        );

        assertThat(assignment.getPlatformCode()).isEqualTo(CustomerAccessProfile.CUSTOMER_PLATFORM_CODE);
        assertThat(assignment.getRoleCode()).isEqualTo(CustomerAccessProfile.CUSTOMER_ROLE_CODE);
        assertThat(assignment.getScope().isGlobal()).isTrue();
    }
}
