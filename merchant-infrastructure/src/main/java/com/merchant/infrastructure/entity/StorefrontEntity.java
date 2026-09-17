package com.merchant.infrastructure.entity;

import com.merchant.domain.enums.StorefrontStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "storefronts", indexes = {
        @Index(name = "idx_storefront_merchant", columnList = "merchant_id"),
        @Index(name = "idx_storefront_status", columnList = "status")
})
public class StorefrontEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private String merchantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorefrontStatus status;

    @Column(name = "lifecycle_reason", length = 1000)
    private String lifecycleReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;
}
