package com.identity.domain.service;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.AccessScope;

import java.util.Objects;


public final class GlobalUserRegistrationAccessPolicy implements RegistrationAccessPolicy{
    private final String platformCode;
    private final String roleCode;

    public GlobalUserRegistrationAccessPolicy(String platformCode, String roleCode) {
        this.platformCode = Objects.requireNonNull(platformCode);
        this.roleCode = Objects.requireNonNull(roleCode);
    }


    @Override
    public AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform) {
        platform.requireSupportedRole(this.roleCode);

        AccessScope globalScope = AccessScope.global();
        return AccessAssignment.create(
                assignmentId,
                userId,
                platform,
                roleCode,
                globalScope,
                null,
                null
        );
    }

    @Override
    public String getPlatformCode() {
        return this.platformCode;
    }
}
