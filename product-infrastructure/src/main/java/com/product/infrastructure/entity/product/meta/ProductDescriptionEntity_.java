package com.product.infrastructure.entity.product.meta;


import com.product.infrastructure.entity.product.entity.ProductDescriptionEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
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
