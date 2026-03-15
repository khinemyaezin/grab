package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Entity
@Table(name = "product_variant")
public class ProductVariantEntity {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String sku;

    @Setter
    @Column(name = "uuid", unique = true)
    private String uuid;

    @Setter
    @Column(name = "status", nullable = false)
    private String status;

    @Setter
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @OneToMany(mappedBy = "productVariant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariationEntity> productVariations = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_variant_media",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<MediaEntity> medias = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productVariant")
    private List<ProductVariantDescriptionEntity> descriptions = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductFeatureEntity> productFeatures = new ArrayList<>();

    public void addProductVariation(ProductVariationEntity entity) {
        entity.setProductVariant(this);
        this.productVariations.add(entity);
    }

    public void removeProductVariantOption(ProductVariationEntity entity) {
        this.productVariations.remove(entity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariantEntity that = (ProductVariantEntity) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, sku);
    }
}
