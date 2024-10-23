package com.product.infrastructure.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product_feature")
public class ProductFeatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariantEntity productVariantEntity;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureEntity featureEntity;

    @ManyToOne
    @JoinColumn(name = "feature_option_id")
    private FeatureOptionEntity featureOptionEntity;

    // getters and setters
}
