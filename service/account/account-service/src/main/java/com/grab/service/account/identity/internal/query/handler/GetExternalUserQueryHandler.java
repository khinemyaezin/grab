package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;
import com.identity.application.port.inbound.GetExternalUserUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetExternalUserQueryHandler
        implements QueryHandler<GetExternalUserQuery, ExternalUserView> {
    private final GetExternalUserUseCase useCase;

    public GetExternalUserQueryHandler(GetExternalUserUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public ExternalUserView handle(GetExternalUserQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<GetExternalUserQuery> getQueryType() {
        return GetExternalUserQuery.class;
    }
}
