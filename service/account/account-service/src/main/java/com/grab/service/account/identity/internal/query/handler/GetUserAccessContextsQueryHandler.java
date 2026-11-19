package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.UserAccessContextsResult;
import com.identity.application.port.inbound.GetUserAccessContextsUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetUserAccessContextsQueryHandler
        implements QueryHandler<GetUserAccessContextsQuery, UserAccessContextsResult> {
    private final GetUserAccessContextsUseCase useCase;

    public GetUserAccessContextsQueryHandler(GetUserAccessContextsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public UserAccessContextsResult handle(GetUserAccessContextsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<GetUserAccessContextsQuery> getQueryType() {
        return GetUserAccessContextsQuery.class;
    }
}
