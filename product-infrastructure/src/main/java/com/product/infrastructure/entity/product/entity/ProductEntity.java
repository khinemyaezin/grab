package com.product.infrastructure.entity.product.entity;

import com.product.infrastructure.entity.category.entity.CategoryEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@Getter
@Entity
@Table(name = "product")
public class ProductEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Setter
    @Column(name = "uuid", unique = true)
    private String uuid;

    @Setter
    @Column
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "product_media", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<MediaEntity> medias = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductDescriptionEntity> descriptions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductVariantEntity> productVariants = new ArrayList<>();

    public void addProductDescription(ProductDescriptionEntity productDescriptionEntity) {
        productDescriptionEntity.setProduct(this);
        descriptions.add(productDescriptionEntity);
    }

    public void addVariant(ProductVariantEntity productVariantEntity) {
        productVariantEntity.setProduct(this);
        productVariants.add(productVariantEntity);
    }

    public void removeVariant(ProductVariantEntity productVariantEntity) {
        productVariants.remove(productVariantEntity);
    }

    public List<ProductVariantEntity> getProductVariants() {
        return Collections.unmodifiableList(productVariants);
    }

    public List<MediaEntity> getMedias() {
        return Collections.unmodifiableList(medias);
    }

    public List<ProductDescriptionEntity> getDescriptions() {
        return Collections.unmodifiableList(descriptions);
    }

}
