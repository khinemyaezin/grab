package com.catalog.application.service;

import com.catalog.application.model.read.ListVariantIdsForProductQuery;
import com.catalog.application.port.inbound.ListVariantIdsForProductUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListVariantIdsForProductService implements ListVariantIdsForProductUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public List<String> execute(ListVariantIdsForProductQuery query) {
        return buyabilityQueryPort.variantIdsForProduct(query.productId());
    }
}
