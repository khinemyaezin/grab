package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.ResolvePlatformUserQuery;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResolvePlatformUserQueryHandler
        implements QueryHandler<ResolvePlatformUserQuery, Optional<AuthenticatedActor>> {

    private final IdentityLookupUseCase identityLookupUseCase;

    public ResolvePlatformUserQueryHandler(IdentityLookupUseCase identityLookupUseCase) {
        this.identityLookupUseCase = identityLookupUseCase;
    }

    @Override
    @IdentityReadTransactional
    public Optional<AuthenticatedActor> handle(ResolvePlatformUserQuery query) {
        return identityLookupUseCase.resolveByPlatformUserId(query.issuer(), query.userId(), query.accessContext());
    }

    @Override
    public Class<ResolvePlatformUserQuery> getQueryType() {
        return ResolvePlatformUserQuery.class;
    }
}

