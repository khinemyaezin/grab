package com.product.infrastructure.entity.product.meta;

import com.product.infrastructure.entity.product.entity.*;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductVariantEntity.class)
public class ProductVariantEntity_ {
    // Define each attribute as volatile for use in Criteria API and MapStruct mappings
    public static volatile SingularAttribute<ProductVariantEntity, Long> id;
    public static volatile SingularAttribute<ProductVariantEntity, String> sku;
    public static volatile SingularAttribute<ProductVariantEntity, String> uuid;
    public static volatile SingularAttribute<ProductVariantEntity, ProductEntity> product;


    // Define SetAttributes for collection-type attributes
    public static volatile SetAttribute<ProductVariantEntity, MediaEntity> medias;
    public static volatile SetAttribute<ProductVariantEntity, ProductVariationEntity> productVariantOptions;
    public static volatile SetAttribute<ProductVariantEntity, ProductFeatureEntity> productFeatures;
    public static volatile SetAttribute<ProductVariantEntity, ProductVariantDescriptionEntity> descriptions;

    // Optional: Define constants for attribute names as type-safe strings
    public static final String ID = "id";
    public static final String SKU = "sku";
    public static final String UUID = "uuid";
    public static final String PRODUCT = "product";
    public static final String MEDIAS = "medias";
    public static final String DESCRIPTIONS = "descriptions";
    public static final String PRODUCT_VARIANT_OPTIONS = "productVariantOptions";
    public static final String PRODUCT_FEATURES = "productFeatures";
}
