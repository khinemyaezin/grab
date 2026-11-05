package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, GetUserProfileResult> {

    private final GetUserProfileUseCase getUserProfileUseCase;

    public GetUserProfileQueryHandler(GetUserProfileUseCase getUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
    }

    @Override
    @IdentityReadTransactional
    public GetUserProfileResult handle(GetUserProfileQuery query) {
        return getUserProfileUseCase.execute(query);
    }

    @Override
    public Class<GetUserProfileQuery> getQueryType() {
        return GetUserProfileQuery.class;
    }
}
