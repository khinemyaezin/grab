package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.SearchZonesUseCase;
import com.inventory.application.model.read.SearchZonesQuery;
import com.inventory.application.model.read.SearchZonesResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchZonesQueryHandler implements QueryHandler<SearchZonesQuery, Page<SearchZonesResult>> {

    private final SearchZonesUseCase searchZonesUseCase;

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
