package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class SellerOidcFailure extends ResponseStatusException {
    private SellerOidcFailure(HttpStatus status, String detail) {
        super(status, detail);
    }

    static SellerOidcFailure invalid() {
        return new SellerOidcFailure(HttpStatus.UNAUTHORIZED, "Sign in to continue");
    }

    static SellerOidcFailure denied() {
        return new SellerOidcFailure(HttpStatus.FORBIDDEN, "The selected context is unavailable");
    }

    static SellerOidcFailure forbidden() {
        return new SellerOidcFailure(HttpStatus.FORBIDDEN, "You do not have access to this operation");
    }

    static SellerOidcFailure busy() {
        return new SellerOidcFailure(HttpStatus.CONFLICT, "The session is being updated");
    }

    static SellerOidcFailure contextRequired() {
        return new SellerOidcFailure(HttpStatus.CONFLICT, "Select an access context to continue");
    }

    public static SellerOidcFailure unavailable() {
        return new SellerOidcFailure(HttpStatus.SERVICE_UNAVAILABLE, "Authentication is temporarily unavailable");
    }

    public static SellerOidcFailure emailExists() {
        return new SellerOidcFailure(HttpStatus.CONFLICT, "A user with this email already exists");
    }

    public static SellerOidcFailure invalidRegistration() {
        return new SellerOidcFailure(HttpStatus.BAD_REQUEST, "Registration details are invalid");
    }
}
