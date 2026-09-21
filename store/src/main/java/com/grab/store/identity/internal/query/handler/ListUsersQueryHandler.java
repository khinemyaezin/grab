package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.port.inbound.ListUsersUseCase;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListUsersQueryHandler implements QueryHandler<ListUsersQuery, Page<ListUsersResult>> {

    private final ListUsersUseCase listUsersUseCase;

    @Override
    @IdentityReadTransactional
    public Page<ListUsersResult> handle(ListUsersQuery query) {
        return listUsersUseCase.execute(query);
    }

    @Override
    public Class<ListUsersQuery> getQueryType() {
        return ListUsersQuery.class;
    }
}
