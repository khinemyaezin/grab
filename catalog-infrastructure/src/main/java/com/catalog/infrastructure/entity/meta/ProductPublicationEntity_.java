package com.catalog.infrastructure.entity.meta;

import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductPublicationEntity.class)
public class ProductPublicationEntity_ {
    public static volatile SingularAttribute<ProductPublicationEntity, Long> productId;
    public static volatile SingularAttribute<ProductPublicationEntity, String> salesChannelId;

    public static final String PRODUCT_ID = "productId";
    public static final String SALES_CHANNEL_ID = "salesChannelId";
}
