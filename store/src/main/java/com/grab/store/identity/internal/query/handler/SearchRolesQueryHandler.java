package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.port.inbound.SearchRolesUseCase;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchRolesQueryHandler implements QueryHandler<SearchRolesQuery, SearchRolesResult> {

    private final SearchRolesUseCase searchRolesUseCase;

    @Override
    @IdentityReadTransactional
    public SearchRolesResult handle(SearchRolesQuery query) {
        return searchRolesUseCase.execute(query);
    }

    @Override
    public Class<SearchRolesQuery> getQueryType() {
        return SearchRolesQuery.class;
    }
}
