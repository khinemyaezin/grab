package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.ResolveExternalIdentityQuery;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResolveExternalIdentityQueryHandler
        implements QueryHandler<ResolveExternalIdentityQuery, Optional<AuthenticatedActor>> {

    private final IdentityLookupUseCase identityLookupUseCase;

    public ResolveExternalIdentityQueryHandler(IdentityLookupUseCase identityLookupUseCase) {
        this.identityLookupUseCase = identityLookupUseCase;
    }

    @Override
    @IdentityReadTransactional
    public Optional<AuthenticatedActor> handle(ResolveExternalIdentityQuery query) {
        return identityLookupUseCase.resolveByExternalIdentity(
                query.issuer(),
                query.subject(),
                query.entitlements(),
                query.accessContext()
        );
    }

    @Override
    public Class<ResolveExternalIdentityQuery> getQueryType() {
        return ResolveExternalIdentityQuery.class;
    }
}

