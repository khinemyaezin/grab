package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record VerifySellerCredentialsQuery(String email, String password) implements Query<VerifiedSeller> {
    @Override
    public String toString() {
        return "VerifySellerCredentialsQuery[redacted]";
    }
}
