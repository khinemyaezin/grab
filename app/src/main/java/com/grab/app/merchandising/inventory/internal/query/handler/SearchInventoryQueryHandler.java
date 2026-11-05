package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.SearchInventoryQuery;
import com.inventory.application.model.read.SearchInventoryResult;
import com.inventory.application.port.inbound.SearchInventoryUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchInventoryQueryHandler implements QueryHandler<SearchInventoryQuery, Page<SearchInventoryResult>> {

    private final SearchInventoryUseCase searchInventoryUseCase;

    public SearchInventoryQueryHandler(SearchInventoryUseCase searchInventoryUseCase) {
        this.searchInventoryUseCase = searchInventoryUseCase;
    }

    @Override
    @InventoryReadTransactional
    public Page<SearchInventoryResult> handle(SearchInventoryQuery query) {
        return searchInventoryUseCase.execute(query);
    }

    @Override
    public Class<SearchInventoryQuery> getQueryType() {
        return SearchInventoryQuery.class;
    }
}
