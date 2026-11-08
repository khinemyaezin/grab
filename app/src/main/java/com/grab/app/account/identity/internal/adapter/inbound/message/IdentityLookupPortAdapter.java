package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ResolveExternalIdentityPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ResolvePlatformUserPortMapper;
import com.grab.app.account.identity.port.message.IdentityLookupPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.ResolveExternalIdentityQuery;
import com.identity.application.model.read.ResolvePlatformUserQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class IdentityLookupPortAdapter implements IdentityLookupPort {

    private final QueryBus queryBus;
    private final ResolvePlatformUserPortMapper platformUserMapper;
    private final ResolveExternalIdentityPortMapper externalIdentityMapper;

    public IdentityLookupPortAdapter(
            @Qualifier("accountQueryBus") QueryBus queryBus,
            ResolvePlatformUserPortMapper platformUserMapper,
            ResolveExternalIdentityPortMapper externalIdentityMapper) {
        this.queryBus = queryBus;
        this.platformUserMapper = platformUserMapper;
        this.externalIdentityMapper = externalIdentityMapper;
    }

    @Override
    public Optional<AuthenticatedActorResult> resolveByPlatformUserId(
            String issuer,
            String userId,
            AccessContextMessage accessContext) {
        ResolvePlatformUserQuery query = platformUserMapper.toQuery(issuer, userId, accessContext);
        Optional<AuthenticatedActor> actor = queryBus.dispatch(query);
        return actor.map(platformUserMapper::toResponse);
    }

    @Override
    public Optional<AuthenticatedActorResult> resolveByExternalIdentity(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContextMessage accessContext) {
        ResolveExternalIdentityQuery query = externalIdentityMapper.toQuery(issuer, subject, entitlements, accessContext);
        Optional<AuthenticatedActor> actor = queryBus.dispatch(query);
        return actor.map(externalIdentityMapper::toResponse);
    }
}
