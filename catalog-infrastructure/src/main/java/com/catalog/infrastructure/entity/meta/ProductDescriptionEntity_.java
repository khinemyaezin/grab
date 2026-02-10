package com.catalog.infrastructure.entity.meta;


import com.catalog.infrastructure.entity.entity.ProductDescriptionEntity;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductDescriptionEntity.class)
public class ProductDescriptionEntity_ {
    public static volatile SingularAttribute<ProductDescriptionEntity, Long> id;
    public static volatile SingularAttribute<ProductDescriptionEntity, String> uuid;
    public static volatile SingularAttribute<ProductDescriptionEntity, String> name;
    public static volatile SingularAttribute<ProductDescriptionEntity, ProductEntity> product;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String PRODUCT = "product";
}
