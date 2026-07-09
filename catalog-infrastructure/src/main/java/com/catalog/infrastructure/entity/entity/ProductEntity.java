package com.catalog.infrastructure.entity.entity;

import com.catalog.domain.valueobject.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "product")
public class ProductEntity implements Serializable {
    @Setter
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
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Getter
    @Setter
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Setter
    @Column(name = "listing_condition")
    private String listingCondition;

    @Setter
    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinTable(name = "product_media", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<MediaEntity> medias = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductDescriptionEntity> descriptions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductVariantEntity> productVariants = new ArrayList<>();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @Setter
    @Column(name = "slug", unique = true)
    private String slug;

    public void addProductDescription(ProductDescriptionEntity productDescriptionEntity) {
        productDescriptionEntity.setProduct(this);
        descriptions.add(productDescriptionEntity);
    }

    public void clearDescriptions() {
        descriptions.clear();
    }

    public void addMedia(MediaEntity mediaEntity) {
        medias.add(mediaEntity);
    }

    public void clearMedias() {
        medias.clear();
    }

    public void addVariant(ProductVariantEntity productVariantEntity) {
        productVariantEntity.setProduct(this);
        productVariants.add(productVariantEntity);
    }

}
