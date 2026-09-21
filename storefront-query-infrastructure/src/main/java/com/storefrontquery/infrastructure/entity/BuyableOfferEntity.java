package com.storefrontquery.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "buyable_offer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_buyable_offer_channel_variant",
                columnNames = {"sales_channel_id", "variant_id"}
        ),
        indexes = {
                @Index(name = "idx_buyable_offer_channel_buyable", columnList = "sales_channel_id, buyable"),
                @Index(name = "idx_buyable_offer_slug", columnList = "sales_channel_id, slug"),
                @Index(name = "idx_buyable_offer_sku", columnList = "sku")
        }
)
public class BuyableOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;

    @Column(name = "variant_id", nullable = false)
    private String variantId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "seller_id")
    private String sellerId;

    @Column
    private String sku;

    @Column
    private String title;

    @Column
    private String slug;

    @Column
    private String media;

    @Column(name = "product_status")
    private String productStatus;

    @Column(nullable = false)
    private boolean published;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column
    private String currency;

    @Column(name = "available_qty", nullable = false)
    private int availableQty;

    @Column(nullable = false)
    private boolean untracked;

    @Column(name = "channel_enabled", nullable = false)
    private boolean channelEnabled = true;

    @Column(nullable = false)
    private boolean buyable;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        recomputeBuyable();
    }

    public void recomputeBuyable() {
        boolean priced = amount != null && currency != null && !currency.isBlank();
        boolean inStock = untracked || availableQty > 0;
        buyable = "ACTIVE".equals(productStatus) && published && priced && inStock && channelEnabled;
    }

    public boolean shouldApply(Instant occurredAt) {
        if (occurredAt == null || updatedAt == null) {
            return true;
        }
        return !occurredAt.isBefore(updatedAt);
    }
}
