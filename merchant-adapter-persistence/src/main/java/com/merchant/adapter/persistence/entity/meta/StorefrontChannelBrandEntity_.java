package com.merchant.adapter.persistence.entity.meta;

import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(StorefrontChannelBrandEntity.class)
public class StorefrontChannelBrandEntity_ {
    public static volatile SingularAttribute<StorefrontChannelBrandEntity, Long> storefrontId;
    public static volatile SingularAttribute<StorefrontChannelBrandEntity, String> salesChannelId;

    public static final String STOREFRONT_ID = "storefrontId";
    public static final String SALES_CHANNEL_ID = "salesChannelId";
}
