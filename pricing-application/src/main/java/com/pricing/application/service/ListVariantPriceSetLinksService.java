package com.pricing.application.service;

import com.pricing.application.port.inbound.ListVariantPriceSetLinksUseCase;

import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import com.pricing.application.model.read.VariantPriceSetLinkResult;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListVariantPriceSetLinksService implements ListVariantPriceSetLinksUseCase {

    private final VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;
    public List<VariantPriceSetLinkResult> execute(ListVariantPriceSetLinksQuery query) {
        return variantPriceSetLinkQueryPort.findByVariantIds(query.variantIds()).stream()
                .map(this::toResult)
                .toList();
    }
private VariantPriceSetLinkResult toResult(VariantPriceSetLinkView view) {
        return new VariantPriceSetLinkResult(
                view.variantId(),
                view.priceSetId(),
                view.productId(),
                view.sku(),
                view.merchantId()
        );
    }
}
