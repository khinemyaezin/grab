package com.catalog.application.service;

import com.catalog.application.model.read.CatalogPublicationItem;
import com.catalog.application.model.read.ListCatalogPublicationsQuery;
import com.catalog.application.port.inbound.ListCatalogPublicationsUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListCatalogPublicationsService implements ListCatalogPublicationsUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public List<CatalogPublicationItem> execute(ListCatalogPublicationsQuery query) {
        return buyabilityQueryPort.listPublications().stream()
                .map(p -> new CatalogPublicationItem(p.variantId(), p.salesChannelId()))
                .toList();
    }
}
