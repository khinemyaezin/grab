package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, GetUserProfileResult> {

    private final GetUserProfileUseCase getUserProfileUseCase;

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
