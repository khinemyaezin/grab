package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.SearchLocationsQuery;
import com.inventory.application.model.read.SearchLocationsResult;
import com.inventory.application.port.inbound.SearchLocationsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchLocationsQueryHandler implements QueryHandler<SearchLocationsQuery, Page<SearchLocationsResult>> {

    private final SearchLocationsUseCase searchLocationsUseCase;

    public SearchLocationsQueryHandler(SearchLocationsUseCase searchLocationsUseCase) {
        this.searchLocationsUseCase = searchLocationsUseCase;
    }

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
