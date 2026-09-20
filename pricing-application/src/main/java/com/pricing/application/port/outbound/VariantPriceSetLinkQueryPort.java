package com.pricing.application.port.outbound;

import com.pricing.application.model.read.VariantPriceSetLinkView;

import java.util.Collection;
import java.util.List;

public interface VariantPriceSetLinkQueryPort {

    List<VariantPriceSetLinkView> findByVariantIds(Collection<String> variantIds);

    List<VariantPriceSetLinkView> findByPriceSetId(String priceSetId);
}
