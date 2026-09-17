package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "catalog_merchant_availability")
public class CatalogMerchantAvailabilityEntity {
    @Id
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String status;

    @Column(name = "merchant_type")
    private String merchantType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
