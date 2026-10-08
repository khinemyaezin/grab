package com.identity.domain.security;

import com.identity.domain.exception.IdentityDomainError;

public record SecurityManifestDecision(SecurityManifestCandidateStatus status, IdentityDomainError error,
                                       boolean newlyActivated, boolean conflict) {
    public String errorCode() {
        return error == null ? null : error.code();
    }
}
