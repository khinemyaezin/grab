package com.identity.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalUserRegistrationAccessPolicyTest {

    @Test
    void constructor_withNullPlatformCode_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> new GlobalUserRegistrationAccessPolicy(null, "ROLE"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_withNullRoleCode_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> new GlobalUserRegistrationAccessPolicy("PLATFORM", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getPlatformCode_withValidInstance_shouldReturnPlatformCode() {
        var policy = new GlobalUserRegistrationAccessPolicy("PLATFORM_A", "ROLE_A");
        assertThat(policy.getPlatformCode()).isEqualTo("PLATFORM_A");
    }

    @Test
    void createAssignment_withSupportedRole_shouldCreateAssignmentWithGlobalScope() {
        var policy = new GlobalUserRegistrationAccessPolicy("CUSTOMER_APP", "CUSTOMER");
        var platform = new Platform(
                new CommonId("platform-1"),
                "CUSTOMER_APP",
                "Customer App",
                true,
                Set.of("CUSTOMER", "GUEST")
        );
        var assignmentId = new CommonId("assign-1");
        var userId = new CommonId("user-1");

        AccessAssignment assignment = policy.createAssignment(assignmentId, userId, platform);

        assertThat(assignment.getId()).isEqualTo(assignmentId);
        assertThat(assignment.getUserId()).isEqualTo(userId);
        assertThat(assignment.getPlatformCode()).isEqualTo("CUSTOMER_APP");
        assertThat(assignment.getRoleCode()).isEqualTo("CUSTOMER");
        assertThat(assignment.getScope().isGlobal()).isTrue();
        assertThat(assignment.getAssignedBy()).isNull();
        assertThat(assignment.getExpiresAt()).isNull();
    }

    @Test
    void createAssignment_withUnsupportedRole_shouldThrowValidationException() {
        var policy = new GlobalUserRegistrationAccessPolicy("CUSTOMER_APP", "ADMIN");
        var platform = new Platform(
                new CommonId("platform-1"),
                "CUSTOMER_APP",
                "Customer App",
                true,
                Set.of("CUSTOMER", "GUEST")
        );
        var assignmentId = new CommonId("assign-1");
        var userId = new CommonId("user-1");

        assertThatThrownBy(() -> policy.createAssignment(assignmentId, userId, platform))
                .isInstanceOf(IdentityDomainValidationException.class)
                .hasMessageContaining("Role is not available on the platform");
    }
}
