package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.event.AccessAssignmentChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.AccessScope;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Getter
public class AccessAssignment extends AggregateRoot<Id> {
    private final Id userId;
    private final String platformCode;
    private final String roleCode;
    private final AccessScope scope;
    private AccessAssignmentStatus status;
    private final Id assignedBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Instant expiresAt;

    public AccessAssignment(
            Id id,
            Id userId,
            String platformCode,
            String roleCode,
            AccessScope scope,
            AccessAssignmentStatus status,
            Id assignedBy,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt
    ) {
        super(id);
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.platformCode = normalizeCode(platformCode, "platform code");
        this.roleCode = normalizeCode(roleCode, "role code");
        this.scope = Objects.requireNonNull(scope, "scope is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.assignedBy = assignedBy;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.expiresAt = expiresAt;
    }

    public static AccessAssignment create(
            Id id,
            Id userId,
            Platform platform,
            String roleCode,
            AccessScope scope,
            Id assignedBy,
            Instant expiresAt
    ) {
        Objects.requireNonNull(platform, "platform is required");
        Objects.requireNonNull(userId, "userId is required");
        if (assignedBy != null && userId.equals(assignedBy)) {
            throw selfAssignmentForbidden();
        }
        String supportedRoleCode = platform.requireSupportedRole(roleCode);
        Instant now = Instant.now();
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw invalidExpiration(expiresAt);
        }
        AccessAssignment assignment = new AccessAssignment(
                id, userId, platform.getCode(), supportedRoleCode, scope,
                AccessAssignmentStatus.ACTIVE, assignedBy, now, now, expiresAt
        );
        assignment.addEvent(new AccessAssignmentChangedEvent(
                id, userId, assignment.platformCode, assignment.roleCode,
                scope.key().value(), scope.scopeId(), null, AccessAssignmentStatus.ACTIVE, now
        ));
        return assignment;
    }

    public void suspend() {
        transitionTo(AccessAssignmentStatus.SUSPENDED, AccessAssignmentStatus.ACTIVE);
    }

    private void reactivate(Id requestedBy) {
        if (userId.equals(Objects.requireNonNull(requestedBy, "requestedBy is required"))) {
            throw selfAssignmentForbidden();
        }
        if (expireIfDue(Instant.now())) {
            return;
        }
        transitionTo(AccessAssignmentStatus.ACTIVE, AccessAssignmentStatus.SUSPENDED);
    }

    public void changeStatus(AccessAssignmentStatus requestedStatus, Id requestedBy) {
        Objects.requireNonNull(requestedStatus, "requestedStatus is required");
        if (requestedStatus == AccessAssignmentStatus.SUSPENDED) {
            suspend();
        } else if (requestedStatus == AccessAssignmentStatus.ACTIVE) {
            reactivate(requestedBy);
        } else if (requestedStatus == AccessAssignmentStatus.REVOKED) {
            revoke();
        } else {
            throw invalidTransition(requestedStatus);
        }
    }

    public boolean expireIfDue(Instant instant) {
        Objects.requireNonNull(instant, "instant is required");
        if ((status == AccessAssignmentStatus.ACTIVE || status == AccessAssignmentStatus.SUSPENDED)
                && expiresAt != null
                && !expiresAt.isAfter(instant)) {
            changeStatus(AccessAssignmentStatus.EXPIRED);
            return true;
        }
        return false;
    }

    public void revoke() {
        if (status == AccessAssignmentStatus.REVOKED) {
            return;
        }
        if (status == AccessAssignmentStatus.EXPIRED) {
            throw invalidTransition(AccessAssignmentStatus.REVOKED);
        }
        changeStatus(AccessAssignmentStatus.REVOKED);
    }

    public boolean isEffectiveAt(Instant instant) {
        Objects.requireNonNull(instant, "instant is required");
        return status == AccessAssignmentStatus.ACTIVE
                && (expiresAt == null || expiresAt.isAfter(instant));
    }

    public AccessAssignmentStatus statusAt(Instant instant) {
        Objects.requireNonNull(instant, "instant is required");
        if ((status == AccessAssignmentStatus.ACTIVE || status == AccessAssignmentStatus.SUSPENDED)
                && expiresAt != null
                && !expiresAt.isAfter(instant)) {
            return AccessAssignmentStatus.EXPIRED;
        }
        return status;
    }

    private void transitionTo(AccessAssignmentStatus requested, AccessAssignmentStatus required) {
        if (status != required) {
            throw invalidTransition(requested);
        }
        changeStatus(requested);
    }

    private void changeStatus(AccessAssignmentStatus requested) {
        AccessAssignmentStatus previous = status;
        status = requested;
        updatedAt = Instant.now();
        addEvent(new AccessAssignmentChangedEvent(
                getId(), userId, platformCode, roleCode,
                scope.key().value(), scope.scopeId(), previous, requested, updatedAt
        ));
    }

    private IdentityDomainValidationException invalidTransition(AccessAssignmentStatus requested) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessAssignmentStatusTransition(status.name(), requested.name()),
                "Access assignment cannot transition from " + status + " to " + requested
        );
    }

    private static IdentityDomainValidationException invalidExpiration(Instant expiresAt) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessExpiration(String.valueOf(expiresAt)),
                "Access expiration must be in the future"
        );
    }

    private static IdentityDomainValidationException selfAssignmentForbidden() {
        return new IdentityDomainValidationException(
                new IdentityDomainError.SelfAccessAssignmentForbidden(),
                "Users cannot grant or reactivate their own access"
        );
    }

    private static String normalizeCode(String value, String label) {
        if (value == null) {
            throw invalidCode(label, null);
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw invalidCode(label, value);
        }
        return normalized;
    }

    private static IdentityDomainValidationException invalidCode(String label, String value) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessCode(label, String.valueOf(value)),
                "Invalid " + label
        );
    }
}
