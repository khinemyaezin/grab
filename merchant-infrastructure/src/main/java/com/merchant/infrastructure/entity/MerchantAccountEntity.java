package com.merchant.infrastructure.entity;

import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "merchant_accounts", indexes = {
        @Index(name = "idx_merchant_applicant", columnList = "applicant_user_id"),
        @Index(name = "idx_merchant_status", columnList = "status")
})
public class MerchantAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "applicant_user_id", nullable = false, updatable = false)
    private String applicantUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "merchant_type", nullable = false, updatable = false)
    private MerchantType type;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "registration_country_code")
    private String registrationCountryCode;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_region")
    private String addressRegion;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_country_code")
    private String addressCountryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status;

    @Column(name = "lifecycle_reason", length = 1000)
    private String lifecycleReason;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;
}
