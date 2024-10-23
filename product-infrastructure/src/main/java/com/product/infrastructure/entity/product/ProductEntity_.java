package com.product.infrastructure.entity.product;

import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductEntity.class)
public class ProductEntity_ {
    public static volatile SingularAttribute<ProductEntity, Long> id;
    public static volatile SetAttribute<ProductEntity, ProductVariantEntity> productVariants;
}
