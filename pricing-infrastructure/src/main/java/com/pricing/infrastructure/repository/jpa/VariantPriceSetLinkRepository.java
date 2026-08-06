package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.view.VariantPriceSetLinkView;

public interface VariantPriceSetLinkRepository {

    void save(VariantPriceSetLinkView link);

    void deleteByVariantId(String variantId);

    void deleteByPriceSetId(String priceSetId);
}
