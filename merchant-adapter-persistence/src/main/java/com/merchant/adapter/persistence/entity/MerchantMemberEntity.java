package com.merchant.adapter.persistence.entity;

import com.merchant.domain.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "merchant_members", indexes = {
        @Index(name = "idx_merchant_members_merchant", columnList = "merchant_id"),
        @Index(name = "idx_merchant_members_user", columnList = "user_id"),
        @Index(name = "idx_merchant_members_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_merchant_member", columnNames = {"merchant_id", "user_id"})
})
public class MerchantMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private String merchantId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, length = 64)
    private String role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "merchant_member_authorities",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "authority", nullable = false, length = 64)
    private Set<String> authorities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MemberStatus status;

    @Column(name = "invited_by")
    private String invitedBy;

    @Column(name = "invitation_expires_at")
    private Instant invitationExpiresAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;
}
