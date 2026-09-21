package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CatalogPublicationItem;
import com.catalog.application.model.read.ListCatalogPublicationsQuery;

import java.util.List;

public interface ListCatalogPublicationsUseCase {
    List<CatalogPublicationItem> execute(ListCatalogPublicationsQuery query);
}
