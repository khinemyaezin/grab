package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import com.identity.application.port.inbound.ListUsersUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListUsersQueryHandler implements QueryHandler<ListUsersQuery, Page<ListUsersResult>> {

    private final ListUsersUseCase listUsersUseCase;

    public ListUsersQueryHandler(ListUsersUseCase listUsersUseCase) {
        this.listUsersUseCase = listUsersUseCase;
    }

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
