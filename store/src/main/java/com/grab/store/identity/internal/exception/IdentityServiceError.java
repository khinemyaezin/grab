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
        IdentityServiceError.RolePlatformBindingInvalid,
        IdentityServiceError.UserNotFound,
        IdentityServiceError.PlatformNotFound,
        IdentityServiceError.PlatformAccessUnavailable,
        IdentityServiceError.AccessAssignmentNotFound,
        IdentityServiceError.AccessAssignmentExists,
        IdentityServiceError.AccessScopeForbidden,
        IdentityServiceError.AccessContextSelectionRequired,
        IdentityServiceError.AccessContextSelectionInvalid,
        IdentityServiceError.AccessInvitationNotFound {

    record RolePlatformBindingInvalid(String roleCode) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.service.role.platform_binding_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("roleCode", roleCode);
        }
    }

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

    record PlatformNotFound(String platformCode) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.platform.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("platformCode", platformCode);
        }
    }

    record PlatformAccessUnavailable(String platformCode) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.service.platform.access_unavailable";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("platformCode", platformCode);
        }
    }

    record AccessAssignmentNotFound(String assignmentId) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.access_assignment.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("assignmentId", assignmentId);
        }
    }

    record AccessAssignmentExists(String userId, String platformCode, String roleCode, String scopeId)
            implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "idt.service.access_assignment.already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "userId", userId,
                    "platformCode", platformCode,
                    "roleCode", roleCode,
                    "scopeId", scopeId
            );
        }
    }

    record AccessScopeForbidden(String scopeKey, String scopeId) implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.service.access.scope_forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("scopeKey", scopeKey, "scopeId", scopeId);
        }
    }

    record AccessContextSelectionRequired(String platformCode, int availableContexts)
            implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "idt.service.access.context_selection_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "platformCode", platformCode,
                    "availableContexts", availableContexts
            );
        }
    }

    record AccessContextSelectionInvalid() implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.service.access.context_selection_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record AccessInvitationNotFound() implements IdentityServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "idt.service.access_invitation.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

}
