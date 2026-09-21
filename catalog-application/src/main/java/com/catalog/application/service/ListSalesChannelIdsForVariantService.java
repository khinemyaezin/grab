package com.catalog.application.service;

import com.catalog.application.model.read.ListSalesChannelIdsForVariantQuery;
import com.catalog.application.port.inbound.ListSalesChannelIdsForVariantUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListSalesChannelIdsForVariantService implements ListSalesChannelIdsForVariantUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public List<String> execute(ListSalesChannelIdsForVariantQuery query) {
        return buyabilityQueryPort.salesChannelIdsForVariant(query.variantId());
    }
}
