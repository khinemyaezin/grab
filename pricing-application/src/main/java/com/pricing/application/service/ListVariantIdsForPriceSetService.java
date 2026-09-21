package com.pricing.application.service;

import com.pricing.application.model.read.ListVariantIdsForPriceSetQuery;
import com.pricing.application.port.inbound.ListVariantIdsForPriceSetUseCase;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListVariantIdsForPriceSetService implements ListVariantIdsForPriceSetUseCase {

    private final VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;

    @Override
    public List<String> execute(ListVariantIdsForPriceSetQuery query) {
        return variantPriceSetLinkQueryPort.findByPriceSetId(query.priceSetId()).stream()
                .map(link -> link.variantId())
                .toList();
    }
}
