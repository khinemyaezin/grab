package com.identity.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface IdentityDomainError extends MessageSource permits
        IdentityDomainError.InvalidEmail,
        IdentityDomainError.InvalidPasswordHash,
        IdentityDomainError.InvalidAuthorityCode,
        IdentityDomainError.InvalidRoleCode,
        IdentityDomainError.InvalidRoleName,
        IdentityDomainError.InvalidSelfRegistrationRole,
        IdentityDomainError.InvalidUserStatusTransition {

    record InvalidEmail(String email) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.email.invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("email", email);
        }
    }

    record InvalidPasswordHash() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.password_hash.invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidRoleCode(String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.role.code_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("roleCode", roleCode);
        }
    }

    record InvalidAuthorityCode(String authorityCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.authority.code_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("authorityCode", authorityCode);
        }
    }

    record InvalidRoleName() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.role.name_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidSelfRegistrationRole(String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.domain.user.self_registration_role_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("roleCode", roleCode);
        }
    }

    record InvalidUserStatusTransition(String currentStatus, String requestedStatus)
            implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.domain.user.status_transition_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }
}
