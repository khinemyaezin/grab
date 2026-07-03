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
        IdentityDomainError.InvalidUserStatusTransition,
        IdentityDomainError.InvalidPlatformCode,
        IdentityDomainError.InvalidPlatformName,
        IdentityDomainError.PlatformRoleNotSupported,
        IdentityDomainError.PlatformAuthorityNotSupported,
        IdentityDomainError.SystemRoleModificationForbidden,
        IdentityDomainError.RoleAuthoritiesRequired,
        IdentityDomainError.AuthoritiesUnavailable,
        IdentityDomainError.RoleNotAssignable,
        IdentityDomainError.InvalidAccessCode,
        IdentityDomainError.InvalidScopeKey,
        IdentityDomainError.InvalidAccessScope,
        IdentityDomainError.AccessScopeNotEncompassed,
        IdentityDomainError.AccessRoleDelegationForbidden,
        IdentityDomainError.SelfAccessAssignmentForbidden,
        IdentityDomainError.InvalidAccessExpiration,
        IdentityDomainError.InvalidAccessAssignmentStatusTransition,
        IdentityDomainError.InvalidAccessInvitationStatusTransition,
        IdentityDomainError.InvalidInvitationTokenHash,
        IdentityDomainError.AccessInvitationExpired,
        IdentityDomainError.SelfAccessInvitationForbidden,
        IdentityDomainError.AccessInvitationRecipientMismatch,
        IdentityDomainError.AccountNotActive {

    record SystemRoleModificationForbidden(String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }

        @Override
        public String code() { return "idt.domain.role.system_modification_forbidden"; }

        @Override
        public Map<String, Object> args() { return Map.of("roleCode", roleCode); }
    }

    record RoleAuthoritiesRequired() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }

        @Override
        public String code() { return "idt.domain.role.authorities_required"; }

        @Override
        public Map<String, Object> args() { return Map.of(); }
    }

    record AuthoritiesUnavailable(java.util.Set<String> authorityCodes) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }

        @Override
        public String code() { return "idt.domain.authority.unavailable"; }

        @Override
        public Map<String, Object> args() { return Map.of("authorityCodes", authorityCodes); }
    }

    record RoleNotAssignable(String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }

        @Override
        public String code() { return "idt.domain.role.not_assignable"; }

        @Override
        public Map<String, Object> args() { return Map.of("roleCode", roleCode); }
    }

    record PlatformAuthorityNotSupported(String platformCode, String authorityCode)
            implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }

        @Override
        public String code() { return "idt.domain.platform.authority_not_supported"; }

        @Override
        public Map<String, Object> args() {
            return Map.of("platformCode", platformCode, "authorityCode", authorityCode);
        }
    }

    record AccountNotActive(String userId) implements IdentityDomainError {
        @Override
        public com.grab.framework.exception.ErrorCategory kind() {
            return com.grab.framework.exception.ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.user.account_not_active";
        }

        @Override
        public java.util.Map<String, Object> args() {
            return java.util.Map.of("userId", userId);
        }
    }

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

    record InvalidPlatformCode(String platformCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.platform.code_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("platformCode", platformCode);
        }
    }

    record InvalidPlatformName() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.platform.name_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record PlatformRoleNotSupported(String platformCode, String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.platform.role_not_supported";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("platformCode", platformCode, "roleCode", roleCode);
        }
    }

    record InvalidAccessCode(String field, String value) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.access.code_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("field", field, "value", value);
        }
    }

    record InvalidScopeKey(String scopeKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.access.scope_key_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("scopeKey", scopeKey);
        }
    }

    record InvalidAccessScope(String scopeKey, String scopeId) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.access.scope_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("scopeKey", scopeKey, "scopeId", scopeId);
        }
    }

    record AccessScopeNotEncompassed(String actorScopeKey, String actorScopeId,
                                     String targetScopeKey, String targetScopeId)
            implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.access.scope_not_encompassed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "actorScopeKey", actorScopeKey,
                    "actorScopeId", actorScopeId,
                    "targetScopeKey", targetScopeKey,
                    "targetScopeId", targetScopeId
            );
        }
    }

    record AccessRoleDelegationForbidden(String roleCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.access.role_delegation_forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("roleCode", roleCode);
        }
    }

    record SelfAccessAssignmentForbidden() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.access.self_assignment_forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidAccessExpiration(String expiresAt) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.access.expiration_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("expiresAt", expiresAt);
        }
    }

    record InvalidAccessAssignmentStatusTransition(String currentStatus, String requestedStatus)
            implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.domain.access.status_transition_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record InvalidAccessInvitationStatusTransition(String currentStatus, String requestedStatus)
            implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.domain.invitation.status_transition_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record InvalidInvitationTokenHash() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "idt.domain.invitation.token_hash_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record AccessInvitationExpired() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "idt.domain.invitation.expired";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record SelfAccessInvitationForbidden() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.invitation.self_invitation_forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record AccessInvitationRecipientMismatch() implements IdentityDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "idt.domain.invitation.recipient_mismatch";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
