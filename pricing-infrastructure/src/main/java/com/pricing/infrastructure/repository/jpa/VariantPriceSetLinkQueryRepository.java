package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.view.VariantPriceSetLinkView;

import java.util.Collection;
import java.util.List;

public interface VariantPriceSetLinkQueryRepository {

    List<VariantPriceSetLinkView> findByVariantIds(Collection<String> variantIds);
}
