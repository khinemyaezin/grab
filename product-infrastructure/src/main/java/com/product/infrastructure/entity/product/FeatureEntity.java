package com.product.infrastructure.entity.product;

import com.product.infrastructure.entity.category.CategoryEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;

@Entity
@Table(name = "feature")
public class FeatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity categoryEntity;

    @OneToMany(mappedBy = "featureEntity")
    private Set<FeatureOptionEntity> featureOptionEntities;

    @OneToMany(mappedBy = "featureEntity")
    private Set<ProductFeatureEntity> productFeatureEntities;

    // getters and setters
}

