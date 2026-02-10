package com.product.infrastructure.entity.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "feature_option")
public class FeatureOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String name;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureEntity feature;

    @OneToMany(mappedBy = "featureOption")
    private Set<ProductFeatureEntity> productFeatures;
}
