package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.port.inbound.SearchBinsUseCase;
import com.inventory.application.model.read.SearchBinsQuery;
import com.inventory.application.model.read.SearchBinsResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchBinsQueryHandler implements QueryHandler<SearchBinsQuery, Page<SearchBinsResult>> {

    private final SearchBinsUseCase searchBinsUseCase;

    @Override
    @InventoryReadTransactional
    public Page<SearchBinsResult> handle(SearchBinsQuery query) {
        return searchBinsUseCase.execute(query);
    }

    @Override
    public Class<SearchBinsQuery> getQueryType() {
        return SearchBinsQuery.class;
    }
}
