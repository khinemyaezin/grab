package com.identity.application.model.read;

import com.identity.domain.enums.UserStatus;

public record CredentialView(String userId, String passwordHash, UserStatus status, long authenticationVersion) {
    @Override
    public String toString() {
        return "CredentialView[redacted]";
    }
}
