package com.merchant.adapter.persistence.mapper.jpa;

import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import com.merchant.adapter.persistence.entity.StorefrontEntity;

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
