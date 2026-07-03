package com.identity.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SellerPlatformUserRegistrationAccessPolicyTest {
    @Test
    void createAssignment_withSupportedRole_shouldCreateAssignmentWithGlobalScope() {
        var policy = new SellerPlatformUserRegistrationAccessPolicy();
        var platform = new Platform(
                new CommonId("platform-1"),
                "SELLER_PORTAL",
                "Seller Platform",
                true,
                Set.of("MERCHANT_APPLICANT", "GUEST")
        );
        var assignmentId = new CommonId("assign-1");
        var userId = new CommonId("user-1");

        AccessAssignment assignment = policy.createAssignment(assignmentId, userId, platform);

        assertThat(assignment.getId()).isEqualTo(assignmentId);
        assertThat(assignment.getUserId()).isEqualTo(userId);
        assertThat(assignment.getPlatformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(assignment.getRoleCode()).isEqualTo("MERCHANT_APPLICANT");
        assertThat(assignment.getScope().isGlobal()).isTrue();
        assertThat(assignment.getAssignedBy()).isNull();
        assertThat(assignment.getExpiresAt()).isNull();
    }

}
