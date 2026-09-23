package com.merchant.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.event.MerchantMemberCreatedEvent;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.MerchantRole;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class MerchantMember extends AggregateRoot<Id> {
    private final Id merchantId;
    private final Id userId;
    private MerchantRole role;
    private MemberStatus status;
    private final Id invitedBy;
    private final Instant invitationExpiresAt;
    private Instant joinedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public MerchantMember(
            Id id,
            Id merchantId,
            Id userId,
            MerchantRole role,
            MemberStatus status,
            Id invitedBy,
            Instant invitationExpiresAt,
            Instant joinedAt,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        super(id);
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.invitedBy = invitedBy;
        this.invitationExpiresAt = invitationExpiresAt;
        this.joinedAt = joinedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
    }

    public static MerchantMember createAdmin(Id id, Id merchantId, Id userId, Instant now) {
        MerchantRole adminRole = MerchantRole.merchantAdmin();
        MerchantMember member = new MerchantMember(
                id,
                merchantId,
                userId,
                adminRole,
                MemberStatus.ACTIVE,
                null,
                null,
                now,
                now,
                now,
                0
        );
        member.addEvent(new MerchantMemberCreatedEvent(
                id.getValue(),
                merchantId.getValue(),
                userId.getValue(),
                adminRole.name(),
                adminRole.authorities(),
                true,
                MemberStatus.ACTIVE.name(),
                null,
                1,
                now
        ));
        return member;
    }

    public static MerchantMember invite(
            Id id,
            Id merchantId,
            Id userId,
            MerchantRole role,
            Id invitedBy,
            Instant expiresAt,
            Instant now
    ) {
        Objects.requireNonNull(role, "role is required");
        if (role.isAdmin()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("role"),
                    "Cannot invite a member directly as MERCHANT_ADMIN"
            );
        }
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("invitationExpiresAt"),
                    "Invitation expiration must be in the future"
            );
        }

        MerchantMember member = new MerchantMember(
                id,
                merchantId,
                userId,
                role,
                MemberStatus.INVITED,
                Objects.requireNonNull(invitedBy, "invitedBy is required"),
                expiresAt,
                null,
                now,
                now,
                0
        );
        member.addEvent(new MerchantMemberCreatedEvent(
                id.getValue(),
                merchantId.getValue(),
                userId.getValue(),
                role.name(),
                role.authorities(),
                false,
                MemberStatus.INVITED.name(),
                invitedBy.getValue(),
                1,
                now
        ));
        return member;
    }

    public void accept(Id actorUserId, Instant now) {
        if (status != MemberStatus.INVITED) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidMemberStatusTransition(status.name(), MemberStatus.ACTIVE.name()),
                    "Member is not in INVITED state"
            );
        }
        if (!userId.equals(actorUserId)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.ApplicantAccessForbidden(merchantId.getValue()),
                    "Only the invited user can accept this invitation"
            );
        }
        if (isExpired(now)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvitationExpired(getId().getValue(), String.valueOf(invitationExpiresAt)),
                    "Invitation has expired"
            );
        }

        this.status = MemberStatus.ACTIVE;
        this.joinedAt = now;
        this.updatedAt = now;
        addEvent(new MerchantMemberRoleChangedEvent(
                getId().getValue(),
                merchantId.getValue(),
                userId.getValue(),
                null,
                role.name(),
                version + 1,
                now
        ));
    }

    public void changeRole(MerchantRole newRole, Instant now) {
        Objects.requireNonNull(newRole, "newRole is required");
        if (status != MemberStatus.ACTIVE) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidMemberStatusTransition(status.name(), "CHANGE_ROLE"),
                    "Cannot change role of a member that is not ACTIVE"
            );
        }
        if (this.role.equals(newRole)) {
            return;
        }

        MerchantRole previousRole = this.role;
        this.role = newRole;
        this.updatedAt = now;
        addEvent(new MerchantMemberRoleChangedEvent(
                getId().getValue(),
                merchantId.getValue(),
                userId.getValue(),
                previousRole.name(),
                newRole.name(),
                version + 1,
                now
        ));
    }

    public void remove(Instant now) {
        if (status == MemberStatus.REMOVED) {
            return;
        }
        this.status = MemberStatus.REMOVED;
        this.updatedAt = now;
        addEvent(new MerchantMemberRemovedEvent(
                getId().getValue(),
                merchantId.getValue(),
                userId.getValue(),
                role.name(),
                version + 1,
                now
        ));
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    public boolean hasAuthority(String authorityCode) {
        return role != null && role.hasAuthority(authorityCode);
    }

    public boolean isExpired(Instant now) {
        return invitationExpiresAt != null && !invitationExpiresAt.isAfter(now);
    }

    public boolean isInvited() {
        return status == MemberStatus.INVITED;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isRemoved() {
        return status == MemberStatus.REMOVED;
    }
}
