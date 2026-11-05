package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.ListProductPublicationsQuery;
import com.catalog.application.model.read.ProductPublicationItem;
import com.catalog.application.port.inbound.ListProductPublicationsUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListProductPublicationsQueryHandler implements QueryHandler<ListProductPublicationsQuery, List<ProductPublicationItem>> {

    private final ListProductPublicationsUseCase listProductPublicationsUseCase;

    public ListProductPublicationsQueryHandler(ListProductPublicationsUseCase listProductPublicationsUseCase) {
        this.listProductPublicationsUseCase = listProductPublicationsUseCase;
    }

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
