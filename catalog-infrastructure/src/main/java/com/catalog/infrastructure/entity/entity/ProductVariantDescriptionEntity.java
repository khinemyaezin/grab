package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_variant_description")
public class ProductVariantDescriptionEntity extends DescriptionSuperEntity {
    @ManyToOne(targetEntity = ProductVariantEntity.class)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariantEntity productVariant;
}
