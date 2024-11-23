package com.product.infrastructure.entity.product.entity;

import com.product.infrastructure.entity.category.CategoryEntity;
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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @OneToMany(mappedBy = "feature")
    private Set<FeatureOptionEntity> featureOptions;

    @OneToMany(mappedBy = "feature")
    private Set<ProductFeatureEntity> productFeatures;

    // getters and setters
}

