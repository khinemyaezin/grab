package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.port.inbound.ListAccessContextsUseCase;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.read.AccessContextResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListAccessContextsQueryHandler implements QueryHandler<ListAccessContextsQuery, List<AccessContextResult>> {

    private final ListAccessContextsUseCase listAccessContextsUseCase;

    @Override
    @IdentityReadTransactional
    public List<AccessContextResult> handle(ListAccessContextsQuery query) {
        return listAccessContextsUseCase.execute(query);
    }

    @Override
    public Class<ListAccessContextsQuery> getQueryType() {
        return ListAccessContextsQuery.class;
    }
}
