package com.identity.infrastructure.entity;

import com.identity.domain.enums.AccessAssignmentStatus;
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
import org.hibernate.annotations.Check;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "access_assignments",
        indexes = {
                @Index(name = "idx_access_assignment_user_platform", columnList = "user_id, platform_role_id"),
                @Index(name = "idx_access_assignment_scope", columnList = "scope_key, scope_id")
        }
)
public class AccessAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_role_id", nullable = false)
    private PlatformRoleEntity platformRole;

    @Column(name = "scope_key", nullable = false)
    private String scopeKey;

    @Column(name = "scope_id", nullable = false)
    private String scopeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessAssignmentStatus status;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
