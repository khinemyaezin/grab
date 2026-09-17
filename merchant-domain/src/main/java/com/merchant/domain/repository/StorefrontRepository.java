package com.merchant.domain.repository;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.valueobject.StorefrontSlug;

import java.util.List;
import java.util.Optional;

public interface StorefrontRepository {
    Optional<Storefront> findById(Id id);

    List<Storefront> findByMerchantId(Id merchantId);

    boolean existsSlug(StorefrontSlug slug, Id excludingId);

    Storefront save(Storefront storefront);
}
