package com.grab.discovery.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "discovery_product_variant",
        uniqueConstraints = @UniqueConstraint(name = "uq_discovery_product_variant", columnNames = {"discovery_product_id", "variant_id"}),
        indexes = {
                @Index(name = "idx_discovery_variant_id", columnList = "variant_id"),
                @Index(name = "idx_discovery_variant_sku", columnList = "sku"),
                @Index(name = "idx_discovery_variant_product_status", columnList = "discovery_product_id, variant_status")
        }
)
public class DiscoveryProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discovery_product_id", nullable = false)
    private DiscoveryProductEntity discoveryProduct;

    @Column(name = "variant_id", nullable = false)
    private String variantId;

    @Column
    private String sku;

    @Column(name = "variant_status", nullable = false, length = 64)
    private String variantStatus;
}
