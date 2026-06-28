package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.enums.InvitationStatus;
import com.identity.domain.event.AccessInvitationChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Getter
public class AccessInvitation extends AggregateRoot<Id> {
    private final Email inviteeEmail;
    private final String platformCode;
    private final String roleCode;
    private final AccessScope scope;
    private final String tokenHash;
    private final Id invitedBy;
    private InvitationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Id acceptedBy;
    private Instant updatedAt;

    public AccessInvitation(
            Id id,
            Email inviteeEmail,
            String platformCode,
            String roleCode,
            AccessScope scope,
            String tokenHash,
            Id invitedBy,
            InvitationStatus status,
            Instant createdAt,
            Instant expiresAt,
            Id acceptedBy,
            Instant updatedAt
    ) {
        super(id);
        this.inviteeEmail = Objects.requireNonNull(inviteeEmail, "inviteeEmail is required");
        this.platformCode = normalizeCode(platformCode);
        this.roleCode = normalizeCode(roleCode);
        this.scope = Objects.requireNonNull(scope, "scope is required");
        this.tokenHash = validateTokenHash(tokenHash);
        this.invitedBy = Objects.requireNonNull(invitedBy, "invitedBy is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
        this.acceptedBy = acceptedBy;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static AccessInvitation create(
            Id id,
            Email inviteeEmail,
            Platform platform,
            String roleCode,
            AccessScope scope,
            String tokenHash,
            Id invitedBy,
            Email inviterEmail,
            Instant expiresAt
    ) {
        Objects.requireNonNull(platform, "platform is required");
        Objects.requireNonNull(inviterEmail, "inviterEmail is required");
        if (Objects.requireNonNull(inviteeEmail, "inviteeEmail is required").equals(inviterEmail)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.SelfAccessInvitationForbidden(),
                    "Users cannot invite themselves"
            );
        }
        String supportedRoleCode = platform.requireSupportedRole(roleCode);
        Instant now = Instant.now();
        if (!expiresAt.isAfter(now)) {
            throw invalidExpiration(expiresAt);
        }
        AccessInvitation invitation = new AccessInvitation(
                id, inviteeEmail, platform.getCode(), supportedRoleCode, scope, tokenHash,
                invitedBy, InvitationStatus.PENDING, now, expiresAt, null, now
        );
        invitation.addEvent(new AccessInvitationChangedEvent(
                id, inviteeEmail.value(), invitation.platformCode, invitation.roleCode,
                scope.key().value(), scope.scopeId(), null, InvitationStatus.PENDING, now
        ));
        return invitation;
    }

    public void accept(Id userId, Email acceptorEmail, Instant now) {
        Objects.requireNonNull(userId, "userId is required");
        if (!inviteeEmail.equals(Objects.requireNonNull(acceptorEmail, "acceptorEmail is required"))) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessInvitationRecipientMismatch(),
                    "Only the intended recipient can accept the invitation"
            );
        }
        ensurePending(now);
        InvitationStatus previous = status;
        status = InvitationStatus.ACCEPTED;
        acceptedBy = userId;
        updatedAt = now;
        addChangedEvent(previous, now);
    }

    public void cancel() {
        if (status != InvitationStatus.PENDING) {
            throw invalidTransition(InvitationStatus.CANCELLED);
        }
        InvitationStatus previous = status;
        status = InvitationStatus.CANCELLED;
        updatedAt = Instant.now();
        addChangedEvent(previous, updatedAt);
    }

    public boolean isPendingAt(Instant now) {
        return status == InvitationStatus.PENDING && expiresAt.isAfter(now);
    }

    private void ensurePending(Instant now) {
        if (status != InvitationStatus.PENDING) {
            throw invalidTransition(InvitationStatus.ACCEPTED);
        }
        if (!expiresAt.isAfter(now)) {
            InvitationStatus previous = status;
            status = InvitationStatus.EXPIRED;
            updatedAt = now;
            addChangedEvent(previous, now);
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.AccessInvitationExpired(),
                    "Access invitation has expired"
            );
        }
    }

    private void addChangedEvent(InvitationStatus previous, Instant occurredAt) {
        addEvent(new AccessInvitationChangedEvent(
                getId(), inviteeEmail.value(), platformCode, roleCode,
                scope.key().value(), scope.scopeId(), previous, status, occurredAt
        ));
    }

    private IdentityDomainValidationException invalidTransition(InvitationStatus requested) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessInvitationStatusTransition(status.name(), requested.name()),
                "Access invitation cannot transition from " + status + " to " + requested
        );
    }

    private static String normalizeCode(String value) {
        if (value == null) {
            throw invalidCode(null);
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw invalidCode(value);
        }
        return normalized;
    }

    private static String validateTokenHash(String value) {
        if (value == null || !value.matches("[a-fA-F0-9]{64}")) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidInvitationTokenHash(),
                    "Invitation token hash is invalid"
            );
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private static IdentityDomainValidationException invalidCode(String value) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessCode("invitation code", String.valueOf(value)),
                "Invalid invitation platform or role code"
        );
    }

    private static IdentityDomainValidationException invalidExpiration(Instant expiresAt) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidAccessExpiration(String.valueOf(expiresAt)),
                "Invitation expiration must be in the future"
        );
    }
}
