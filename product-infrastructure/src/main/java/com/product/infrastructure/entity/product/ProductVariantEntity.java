package com.product.infrastructure.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
public class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity productEntity;

    @OneToMany(mappedBy = "productVariantEntity",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ProductVariantOptionEntity> productVariantOptionEntities = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "media_id")
    private MediaEntity mediaEntity;

    private String about;

    @OneToMany(mappedBy = "productVariantEntity")
    private Set<ProductFeatureEntity> productFeatureEntities;

    @OneToMany(mappedBy = "productVariantEntity")
    private Set<PriceEntity> priceEntities;


}
