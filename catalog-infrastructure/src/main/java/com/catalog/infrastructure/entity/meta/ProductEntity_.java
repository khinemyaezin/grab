package com.catalog.infrastructure.entity.meta;


import com.catalog.infrastructure.entity.entity.MediaEntity;
import com.catalog.infrastructure.entity.entity.ProductDescriptionEntity;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductEntity.class)
public class ProductEntity_ {
    public static volatile SingularAttribute<ProductEntity, Long> id;
    public static volatile SingularAttribute<ProductEntity, String> uuid;
    public static volatile SingularAttribute<ProductEntity, String> name;
    public static volatile SingularAttribute<ProductEntity, String> categoryId;
    public static volatile SingularAttribute<ProductEntity, String> listingCondition;
    public static volatile SetAttribute<ProductEntity, MediaEntity> medias;
    public static volatile SetAttribute<ProductEntity, ProductDescriptionEntity> descriptions;
    public static volatile SetAttribute<ProductEntity, ProductVariantEntity> productVariants;
    public static volatile SingularAttribute<ProductEntity, String> status;
    public static volatile SingularAttribute<ProductEntity, String> slug;
    public static volatile SingularAttribute<ProductEntity, Boolean> featured;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String CATEGORY_ENTITY = "categoryId";
    public static final String LISTING_CONDITION = "listingCondition";
    public static final String MEDIA_ENTITIES = "medias";
    public static final String DESCRIPTION_ENTITIES = "descriptions";
    public static final String PRODUCT_VARIANT_ENTITIES = "productVariants";
    public static final String STATUS = "status";
    public static final String SLUG = "slug";
}
