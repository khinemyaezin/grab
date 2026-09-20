package com.pricing.domain.port.outbound;

import com.pricing.domain.valueobject.VariantPriceSetLink;

import java.util.Optional;

public interface VariantPriceSetLinkRepository {

    Optional<VariantPriceSetLink> findByVariantId(String variantId);

    void save(VariantPriceSetLink link);

    void deleteByVariantId(String variantId);

    void deleteByPriceSetId(String priceSetId);
}
