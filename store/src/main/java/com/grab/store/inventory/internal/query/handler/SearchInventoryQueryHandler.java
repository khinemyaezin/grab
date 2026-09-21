package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.SearchInventoryUseCase;
import com.inventory.application.model.read.SearchInventoryQuery;
import com.inventory.application.model.read.SearchInventoryResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchInventoryQueryHandler implements QueryHandler<SearchInventoryQuery, Page<SearchInventoryResult>> {

    private final SearchInventoryUseCase searchInventoryUseCase;

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
