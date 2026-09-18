package com.catalog.infrastructure.entity.meta;

import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProductPublicationEntity.class)
public class ProductPublicationEntity_ {
    public static volatile SingularAttribute<ProductPublicationEntity, Long> variantId;
    public static volatile SingularAttribute<ProductPublicationEntity, String> salesChannelId;

    public static final String VARIANT_ID = "variantId";
    public static final String SALES_CHANNEL_ID = "salesChannelId";
}
