package com.grab.store.shared.security.expection;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface IdentitySecurityError extends MessageSource permits
        IdentitySecurityError.AccountNotActive,
        IdentitySecurityError.IdentityNotLinked,
        IdentitySecurityError.InvalidCredentials,
        IdentitySecurityError.InvalidRefreshToken,
        IdentitySecurityError.InvalidTokenAudience,
        IdentitySecurityError.InvalidTokenType,
        IdentitySecurityError.TokenExpired,
        IdentitySecurityError.InvalidToken,
        IdentitySecurityError.MalformedToken,
        IdentitySecurityError.MissingToken {

    record AccountNotActive() implements IdentitySecurityError {
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

    record InvalidCredentials() implements IdentitySecurityError {
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

    record InvalidRefreshToken() implements IdentitySecurityError {
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

    record InvalidTokenAudience() implements IdentitySecurityError {
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

    record InvalidTokenType() implements IdentitySecurityError {
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

    record TokenExpired() implements IdentitySecurityError {
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

    record InvalidToken() implements IdentitySecurityError {
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

    record MalformedToken() implements IdentitySecurityError {
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

    record MissingToken() implements IdentitySecurityError {
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
}
