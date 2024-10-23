package com.product.infrastructure.entity.product;

import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductVariantEntity.class)
public class ProductVariantEntity_ {
    public static volatile SingularAttribute<ProductVariantEntity, Long> id;
    public static volatile SetAttribute<ProductVariantEntity, ProductVariantOptionEntity> productVariantOptions;
}
