package com.product.infrastructure.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;

@Entity
@Table(name = "media")
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    private String type;

    @Column(unique = true, nullable = false)
    private String path;

    @OneToMany(mappedBy = "mediaEntity")
    private Set<ProductEntity> productEntities;

    @OneToMany(mappedBy = "mediaEntity")
    private Set<ProductVariantEntity> productVariantEntities;

    // getters and setters
}
