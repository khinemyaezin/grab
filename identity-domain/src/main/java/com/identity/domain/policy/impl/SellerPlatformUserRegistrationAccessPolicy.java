package com.identity.domain.policy.impl;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.policy.RegistrationAccessPolicy;
import com.identity.domain.service.MerchantAccessProfile;
import com.identity.domain.valueobject.AccessScope;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public final class SellerPlatformUserRegistrationAccessPolicy implements RegistrationAccessPolicy {

    @Override
    public String platformCode() {
        return MerchantAccessProfile.SELLER_PLATFORM_CODE;
    }

    @Override
    public AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform) {
        platform.requireSupportedRole(MerchantAccessProfile.APPLICANT_ROLE_CODE);

        AccessScope globalScope = AccessScope.global();
        return AccessAssignment.create(
                assignmentId,
                userId,
                platform,
                MerchantAccessProfile.APPLICANT_ROLE_CODE,
                globalScope,
                null,
                null
        );
    }
}
