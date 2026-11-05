package com.grab.app.merchandising.inventory.internal.query.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.application.model.read.SearchBinsQuery;
import com.inventory.application.model.read.SearchBinsResult;
import com.inventory.application.port.inbound.SearchBinsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchBinsQueryHandler implements QueryHandler<SearchBinsQuery, Page<SearchBinsResult>> {

    private final SearchBinsUseCase searchBinsUseCase;

    public SearchBinsQueryHandler(SearchBinsUseCase searchBinsUseCase) {
        this.searchBinsUseCase = searchBinsUseCase;
    }

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
