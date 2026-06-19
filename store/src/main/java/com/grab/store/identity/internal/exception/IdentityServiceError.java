package com.grab.store.identity.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface IdentityServiceError extends MessageSource permits
        IdentityServiceError.AuthorityNotFound,
        IdentityServiceError.EmailExists,
        IdentityServiceError.InvalidRole,
        IdentityServiceError.InvalidStatusTransition,
        IdentityServiceError.RoleExists,
        IdentityServiceError.RoleNotFound,
        IdentityServiceError.UserNotFound {

    record EmailExists(String email) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "idt.service.user.email_already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("email", email);
        }
    }

    record InvalidRole(String role) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.service.user.invalid_role";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("role", role);
        }
    }

    record UserNotFound(String id) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.user.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("userId", id);
        }
    }

    record RoleNotFound(String role) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.role.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("role", role);
        }
    }

    record RoleExists(String role) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "idt.service.role.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("role", role);
        }
    }

    record AuthorityNotFound(String authority) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.authority.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("authority", authority);
        }
    }

    record InvalidStatusTransition(String currentStatus, String requestedStatus)
            implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.service.user.status_transition_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }
}
