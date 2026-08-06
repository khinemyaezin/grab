package com.pricing.infrastructure.entity;

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
@Table(name = "variant_price_set_links")
public class VariantPriceSetLinkEntity {

    @Id
    @Column(name = "variant_id", nullable = false, length = 64)
    private String variantId;

    @Column(name = "price_set_id", nullable = false, length = 64)
    private String priceSetId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
