package com.grab.discovery.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "discovery_product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_discovery_product_channel_product", columnNames = {"sales_channel_id", "product_id"}),
                @UniqueConstraint(name = "uq_discovery_product_channel_slug", columnNames = {"sales_channel_id", "slug"})
        },
        indexes = {
                @Index(name = "idx_discovery_product_channel_published", columnList = "sales_channel_id, published"),
                @Index(name = "idx_discovery_product_slug", columnList = "sales_channel_id, slug")
        }
)
public class DiscoveryProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "seller_id")
    private String sellerId;

    @Column
    private String title;

    @Column
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private String thumbnail;

    @Column(name = "product_status")
    private String productStatus;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "channel_enabled", nullable = false)
    private boolean channelEnabled;

    @Column(name = "has_stock", nullable = false)
    private boolean hasStock;

    @Column
    private String currency;

    @Column(name = "min_price", precision = 19, scale = 4)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 19, scale = 4)
    private BigDecimal maxPrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<DiscoveryProductOptionDocument> options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<DiscoveryVariantOfferDocument> variants;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
