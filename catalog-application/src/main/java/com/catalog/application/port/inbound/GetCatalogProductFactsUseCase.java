package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;

import java.util.Optional;

public interface GetCatalogProductFactsUseCase {
    Optional<GetCatalogProductFactsResult> execute(GetCatalogProductFactsQuery query);
}
