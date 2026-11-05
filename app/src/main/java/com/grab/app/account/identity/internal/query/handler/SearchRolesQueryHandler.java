package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import com.identity.application.port.inbound.SearchRolesUseCase;
import org.springframework.stereotype.Component;

@Component
public class SearchRolesQueryHandler implements QueryHandler<SearchRolesQuery, SearchRolesResult> {

    private final SearchRolesUseCase searchRolesUseCase;

    public SearchRolesQueryHandler(SearchRolesUseCase searchRolesUseCase) {
        this.searchRolesUseCase = searchRolesUseCase;
    }

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
