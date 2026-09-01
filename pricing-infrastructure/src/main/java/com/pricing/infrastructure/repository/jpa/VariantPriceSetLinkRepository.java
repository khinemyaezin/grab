package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.view.VariantPriceSetLinkView;

import java.util.Optional;

public interface VariantPriceSetLinkRepository {

    Optional<VariantPriceSetLinkView> findByVariantId(String variantId);

    void save(VariantPriceSetLinkView link);

    void deleteByVariantId(String variantId);

    void deleteByPriceSetId(String priceSetId);
}
