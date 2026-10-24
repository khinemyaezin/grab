package com.grab.discovery.adapter.persistence.entity;

import com.grab.discovery.adapter.persistence.converter.DiscoveryVariantFactsConverter;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection.ProductOption;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection.VariantFact;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "category_id")
    private String categoryId;

    @Column
    private String title;

    @Column
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 2048)
    private String thumbnail;

    @Column(name = "product_status", length = 64)
    private String productStatus;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "channel_enabled", nullable = false)
    private boolean channelEnabled;

    @Column(name = "has_stock", nullable = false)
    private boolean hasStock;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<ProductOption> options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = DiscoveryVariantFactsConverter.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<VariantFact> variants;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
