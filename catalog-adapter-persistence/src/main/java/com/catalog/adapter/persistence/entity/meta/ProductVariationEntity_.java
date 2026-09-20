package com.catalog.adapter.persistence.entity.meta;

import com.catalog.adapter.persistence.entity.ProductVariationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductVariationEntity.class)
public class ProductVariationEntity_ {
    public static volatile SingularAttribute<ProductVariationEntity, ProductVariationEntity.ProductVariationId> id;
    public static volatile SingularAttribute<ProductVariationEntity, ProductVariantEntity> productVariant;

    public static final String ID = "id";
    public static final String PRODUCT_VARIANT = "productVariant";
}
