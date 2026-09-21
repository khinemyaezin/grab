package com.pricing.application.service;

import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.ListPriceListsUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.model.read.ListPriceListsQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPriceListsService implements ListPriceListsUseCase {

    private final PriceQueryPort priceQueryPort;

    public List<PriceListResult> execute(ListPriceListsQuery query) {
        return priceQueryPort.findAllPriceLists();
    }
}
