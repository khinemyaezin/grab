package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.IdentityLookupPort.AccessContextMessage;
import com.grab.app.account.identity.port.message.IdentityLookupPort.AuthenticatedActorResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.ResolveExternalIdentityQuery;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolveExternalIdentityPortMapper {

    public ResolveExternalIdentityQuery toQuery(
            String issuer,
            String subject,
            Set<String> entitlements,
            AccessContextMessage accessContext) {
        AccessContext ctx = toContext(accessContext);
        return new ResolveExternalIdentityQuery(issuer, subject, entitlements, ctx);
    }

    public abstract AuthenticatedActorResult toResponse(AuthenticatedActor actor);

    protected abstract AccessContext toContext(AccessContextMessage message);
}

