package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "feature")
public class FeatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String name;

    @Column(name = "category_id")
    private Long categoryId;

    @OneToMany(mappedBy = "feature")
    private Set<FeatureOptionEntity> featureOptions;

    @OneToMany(mappedBy = "feature")
    private Set<ProductFeatureEntity> productFeatures;

    // getters and setters
}

