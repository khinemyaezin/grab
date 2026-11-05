package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import com.identity.application.port.inbound.ListRolesUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ListRolesQueryHandler implements QueryHandler<ListRolesQuery, Page<ListRolesResult>> {

    private final ListRolesUseCase listRolesUseCase;

    public ListRolesQueryHandler(ListRolesUseCase listRolesUseCase) {
        this.listRolesUseCase = listRolesUseCase;
    }

    @Override
    @IdentityReadTransactional
    public Page<ListRolesResult> handle(ListRolesQuery query) {
        return listRolesUseCase.execute(query);
    }

    @Override
    public Class<ListRolesQuery> getQueryType() {
        return ListRolesQuery.class;
    }
}
