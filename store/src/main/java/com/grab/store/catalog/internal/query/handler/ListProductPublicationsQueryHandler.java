package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.ListProductPublicationsUseCase;
import com.catalog.application.model.read.ListProductPublicationsQuery;
import com.catalog.application.model.read.ProductPublicationItem;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListProductPublicationsQueryHandler
        implements QueryHandler<ListProductPublicationsQuery, List<ProductPublicationItem>> {

    private final ListProductPublicationsUseCase listProductPublicationsUseCase;

    @Override
    @CatalogReadTransactional
    public List<ProductPublicationItem> handle(ListProductPublicationsQuery query) {
        return listProductPublicationsUseCase.execute(query);
    }

    @Override
    public Class<ListProductPublicationsQuery> getQueryType() {
        return ListProductPublicationsQuery.class;
    }
}
