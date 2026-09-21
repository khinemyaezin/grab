package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListVariantIdsForProductQuery;

import java.util.List;

public interface ListVariantIdsForProductUseCase {
    List<String> execute(ListVariantIdsForProductQuery query);
}
