package com.identity.infrastructure.entity;

import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.enums.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "access_invitations",
        indexes = {
                @Index(name = "idx_access_invitation_email", columnList = "invitee_email"),
                @Index(name = "idx_access_invitation_scope", columnList = "scope_type, scope_id")
        }
)
public class AccessInvitationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_role_id", nullable = false)
    private PlatformRoleEntity platformRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private AccessScopeType scopeType;

    @Column(name = "scope_id", nullable = false)
    private String scopeId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "invited_by", nullable = false)
    private String invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_by")
    private String acceptedBy;
}
