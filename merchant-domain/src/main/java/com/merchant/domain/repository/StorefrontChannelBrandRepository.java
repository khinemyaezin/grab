package com.merchant.domain.repository;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.StorefrontChannelBrand;

import java.util.Optional;

public interface StorefrontChannelBrandRepository {
    Optional<StorefrontChannelBrand> findByStorefrontId(Id storefrontId);

    boolean existsByStorefrontId(Id storefrontId);

    void save(StorefrontChannelBrand brand);

    void delete(StorefrontChannelBrand brand);
}
