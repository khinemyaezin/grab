package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.SearchLocationsUseCase;
import com.inventory.application.model.read.SearchLocationsQuery;
import com.inventory.application.model.read.SearchLocationsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLocationsQueryHandler implements QueryHandler<SearchLocationsQuery, Page<SearchLocationsResult>> {

    private final SearchLocationsUseCase searchLocationsUseCase;

    @Override
    @InventoryReadTransactional
    public Page<SearchLocationsResult> handle(SearchLocationsQuery query) {
        return searchLocationsUseCase.execute(query);
    }

    @Override
    public Class<SearchLocationsQuery> getQueryType() {
        return SearchLocationsQuery.class;
    }
}
