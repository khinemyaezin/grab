package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.SearchZonesQuery;
import com.inventory.application.model.read.SearchZonesResult;
import com.inventory.application.port.inbound.SearchZonesUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchZonesQueryHandler implements QueryHandler<SearchZonesQuery, Page<SearchZonesResult>> {

    private final SearchZonesUseCase searchZonesUseCase;

    public SearchZonesQueryHandler(SearchZonesUseCase searchZonesUseCase) {
        this.searchZonesUseCase = searchZonesUseCase;
    }

    @Override
    @InventoryReadTransactional
    public Page<SearchZonesResult> handle(SearchZonesQuery query) {
        return searchZonesUseCase.execute(query);
    }

    @Override
    public Class<SearchZonesQuery> getQueryType() {
        return SearchZonesQuery.class;
    }
}
