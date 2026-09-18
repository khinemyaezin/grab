package com.merchant.infrastructure.mapper.jpa;

import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.infrastructure.entity.StorefrontChannelBrandEntity;
import com.merchant.infrastructure.entity.StorefrontEntity;

public interface StorefrontChannelBrandJpaAssembler {

    StorefrontChannelBrandEntity buildFullEntityGraph(
            StorefrontChannelBrand brand,
            StorefrontChannelBrandEntity entity,
            StorefrontEntity storefront
    );

    StorefrontChannelBrand toFullDomainGraph(
            StorefrontChannelBrandEntity entity,
            StorefrontEntity storefront
    );
}
