package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SellerSecurityError extends MessageSource permits
        SellerSecurityError.AuthenticationSessionExpired,
        SellerSecurityError.AccountNotActive,
        SellerSecurityError.IdentityNotLinked,
        SellerSecurityError.InvalidCredentials,
        SellerSecurityError.InvalidRefreshToken,
        SellerSecurityError.InvalidTokenAudience,
        SellerSecurityError.InvalidTokenType,
        SellerSecurityError.TokenExpired,
        SellerSecurityError.InvalidToken,
        SellerSecurityError.MalformedToken,
        SellerSecurityError.MissingToken,
        SellerSecurityError.InvalidAccessContext,
        SellerSecurityError.ContextRequired {

    record AuthenticationSessionExpired() implements SellerSecurityError {
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

    record AccountNotActive() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.account_not_active";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record IdentityNotLinked() implements SellerSecurityError {
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

    record InvalidCredentials() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_credentials";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidRefreshToken() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_refresh";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidTokenAudience() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidTokenType() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record TokenExpired() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.expired_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidToken() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.invalid_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record MalformedToken() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.malformed_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record MissingToken() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "idt.service.auth.missing_token";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidAccessContext() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.service.auth.access_context_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record ContextRequired() implements SellerSecurityError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "idt.service.auth.access_context_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}

