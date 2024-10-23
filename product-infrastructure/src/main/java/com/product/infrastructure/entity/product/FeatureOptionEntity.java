package com.product.infrastructure.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;


@Entity
@Table(name = "feature_option")
public class FeatureOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    private String name;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureEntity featureEntity;

    @OneToMany(mappedBy = "featureOptionEntity")
    private Set<ProductFeatureEntity> productFeatureEntities;

    // getters and setters
}
