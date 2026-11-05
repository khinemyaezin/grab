package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.port.inbound.ListAccessContextsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAccessContextsQueryHandler
        implements QueryHandler<ListAccessContextsQuery, List<AccessContextResult>> {

    private final ListAccessContextsUseCase useCase;

    public ListAccessContextsQueryHandler(ListAccessContextsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public List<AccessContextResult> handle(ListAccessContextsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListAccessContextsQuery> getQueryType() {
        return ListAccessContextsQuery.class;
    }
}
