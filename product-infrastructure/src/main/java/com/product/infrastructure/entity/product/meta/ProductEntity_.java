package com.product.infrastructure.entity.product.meta;


import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.MediaEntity;
import com.product.infrastructure.entity.product.entity.ProductDescriptionEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductEntity.class)
public class ProductEntity_ {
    public static volatile SingularAttribute<ProductEntity, Long> id;
    public static volatile SingularAttribute<ProductEntity, String> uuid;
    public static volatile SingularAttribute<ProductEntity, String> name;
    public static volatile SingularAttribute<ProductEntity, CategoryEntity> category;
    public static volatile SetAttribute<ProductEntity, MediaEntity> medias;
    public static volatile SetAttribute<ProductEntity, ProductDescriptionEntity> descriptions;
    public static volatile SetAttribute<ProductEntity, ProductVariantEntity> productVariants;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String CATEGORY_ENTITY = "category";
    public static final String MEDIA_ENTITIES = "medias";
    public static final String DESCRIPTION_ENTITIES = "descriptions";
    public static final String PRODUCT_VARIANT_ENTITIES = "productVariants";
}
