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
        IdentityDomainError.AccountNotActive,
        IdentityDomainError.SecurityManifestDigestMismatch,
        IdentityDomainError.SecurityManifestPayloadConflict,
        IdentityDomainError.SecurityManifestRevisionConflict,
        IdentityDomainError.SecurityManifestStaleRevision,
        IdentityDomainError.SecurityManifestLowerThanPending,
        IdentityDomainError.SecurityManifestMissingDependency,
        IdentityDomainError.SecurityManifestOmittedAuthority,
        IdentityDomainError.SecurityManifestUnsupportedSchema,
        IdentityDomainError.SecurityManifestProtectedModule,
        IdentityDomainError.SecurityManifestInvalidScopeParent,
        IdentityDomainError.SecurityManifestUnknownParentScope,
        IdentityDomainError.SecurityManifestScopeOwnerConflict,
        IdentityDomainError.SecurityManifestScopeCycle,
        IdentityDomainError.SecurityManifestAuthorityOwnerConflict,
        IdentityDomainError.SecurityManifestOmittedScope,
        IdentityDomainError.SecurityManifestReactivation,
        IdentityDomainError.SecurityManifestInvalidDependency {

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

    record SecurityManifestDigestMismatch(String expected, String actual) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.digest_mismatch"; }
        @Override
        public Map<String, Object> args() { return Map.of("expected", expected, "actual", actual); }
    }

    record SecurityManifestPayloadConflict(String eventId) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.event_payload_conflict"; }
        @Override
        public Map<String, Object> args() { return Map.of("eventId", eventId); }
    }

    record SecurityManifestRevisionConflict(String moduleKey, int revision) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.revision_payload_conflict"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey, "revision", revision); }
    }

    record SecurityManifestStaleRevision(String moduleKey, int revision, int appliedRevision) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.stale_revision"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey, "revision", revision, "appliedRevision", appliedRevision); }
    }

    record SecurityManifestLowerThanPending(String moduleKey, int revision, int pendingRevision) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.lower_than_pending"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey, "revision", revision, "pendingRevision", pendingRevision); }
    }

    record SecurityManifestMissingDependency(String dependencyKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        @Override
        public String code() { return "idt.domain.security_manifest.missing_dependency"; }
        @Override
        public Map<String, Object> args() { return Map.of("dependencyKey", dependencyKey); }
    }

    record SecurityManifestOmittedAuthority(String moduleKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.omitted_authority"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey); }
    }

    record SecurityManifestUnsupportedSchema(int schemaVersion) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.unsupported_schema"; }
        @Override
        public Map<String, Object> args() { return Map.of("schemaVersion", schemaVersion); }
    }

    record SecurityManifestProtectedModule(String moduleKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.FORBIDDEN; }
        @Override
        public String code() { return "idt.domain.security_manifest.protected_module"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey); }
    }

    record SecurityManifestInvalidScopeParent(String scopeKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.invalid_scope_parent"; }
        @Override
        public Map<String, Object> args() { return Map.of("scopeKey", scopeKey); }
    }

    record SecurityManifestUnknownParentScope(String parentScopeKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.unknown_parent_scope"; }
        @Override
        public Map<String, Object> args() { return Map.of("parentScopeKey", parentScopeKey); }
    }

    record SecurityManifestScopeOwnerConflict(String scopeKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.scope_owner_conflict"; }
        @Override
        public Map<String, Object> args() { return Map.of("scopeKey", scopeKey); }
    }

    record SecurityManifestScopeCycle(String scopeKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        @Override
        public String code() { return "idt.domain.security_manifest.scope_cycle"; }
        @Override
        public Map<String, Object> args() { return Map.of("scopeKey", scopeKey); }
    }

    record SecurityManifestAuthorityOwnerConflict(String authorityCode) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.authority_owner_conflict"; }
        @Override
        public Map<String, Object> args() { return Map.of("authorityCode", authorityCode); }
    }
    record SecurityManifestOmittedScope(String moduleKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.omitted_scope"; }
        @Override
        public Map<String, Object> args() { return Map.of("moduleKey", moduleKey); }
    }

    record SecurityManifestReactivation(String key) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override
        public String code() { return "idt.domain.security_manifest.reactivation"; }
        @Override
        public Map<String, Object> args() { return Map.of("key", key); }
    }

    record SecurityManifestInvalidDependency(String dependencyKey) implements IdentityDomainError {
        @Override
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override
        public String code() { return "idt.domain.security_manifest.invalid_dependency"; }
        @Override
        public Map<String, Object> args() { return Map.of("dependencyKey", dependencyKey); }
    }

}
