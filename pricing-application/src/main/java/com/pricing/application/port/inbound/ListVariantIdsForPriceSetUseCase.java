package com.pricing.application.port.inbound;

import com.pricing.application.model.read.ListVariantIdsForPriceSetQuery;

import java.util.List;

public interface ListVariantIdsForPriceSetUseCase {
    List<String> execute(ListVariantIdsForPriceSetQuery query);
}
