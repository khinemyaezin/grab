package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;

import java.util.Optional;

public interface GetCatalogVariantFactsUseCase {
    Optional<GetCatalogProductFactsResult> execute(GetCatalogVariantFactsQuery query);
}
