package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_feature")
public class ProductFeatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariantEntity productVariant;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureEntity feature;

    @ManyToOne
    @JoinColumn(name = "feature_option_id")
    private FeatureOptionEntity featureOption;

    // getters and setters
}
