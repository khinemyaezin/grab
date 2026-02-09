package com.product.infrastructure.entity.product.meta;

import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductVariationEntity.class)
public class ProductVariationEntity_ {
    public static volatile SingularAttribute<ProductVariationEntity, ProductVariationEntity.ProductVariationId> id;
    public static volatile SingularAttribute<ProductVariationEntity, ProductVariantEntity> productVariant;
    public static volatile SingularAttribute<ProductVariationEntity, String> variantOptionValue;
    public static volatile SingularAttribute<ProductVariationEntity, String> variantTypeValue;

    public static final String ID = "id";
    public static final String PRODUCT_VARIANT = "productVariant";
    public static final String VARIANT_OPTION_VALUE = "variantOptionValue";
    public static final String VARIANT_TYPE_VALUE = "variantTypeValue";
}
