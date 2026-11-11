package com.grab.app.account.identity.internal.adapter.outbound.security.error;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface IdentitySecurityError extends MessageSource permits
        IdentitySecurityError.InvalidRefreshToken,
        IdentitySecurityError.AuthenticationSessionExpired,
        IdentitySecurityError.IdentityNotLinked {

    record InvalidRefreshToken() implements IdentitySecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_refresh_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record AuthenticationSessionExpired() implements IdentitySecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.session_expired";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record IdentityNotLinked() implements IdentitySecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.identity_not_linked";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}

