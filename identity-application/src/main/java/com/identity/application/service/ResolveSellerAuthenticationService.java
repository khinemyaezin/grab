package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAuthenticationResult;
import com.identity.application.port.inbound.ResolveSellerAuthenticationUseCase;
import com.identity.application.port.outbound.SellerIdentityStateReader;
import com.identity.application.port.outbound.SellerScopeLifecyclePort;
import com.identity.application.port.outbound.SellerScopeLifecyclePort.ScopeRevision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ResolveSellerAuthenticationService implements ResolveSellerAuthenticationUseCase {
    private final SellerIdentityStateReader identities;
    private final SellerScopeLifecyclePort scopes;
    private final IdGenerator ids;

    public ResolveSellerAuthenticationService(SellerIdentityStateReader identities, SellerScopeLifecyclePort scopes,
                                              IdGenerator ids) {
        this.identities = identities;
        this.scopes = scopes;
        this.ids = ids;
    }

    @Override
    public SellerAuthenticationResult execute(ResolveSellerAuthenticationQuery query) {
        if (query.userId() == null) {
            return null;
        }
        if (!query.selectSingleContext()) {
            return resolve(query.userId(), query.assignmentId());
        }
        if (query.assignmentId() != null) {
            return null;
        }
        List<AccessContextResult> available = identities.listContexts(query.userId());
        List<SellerAuthenticationResult> valid = new ArrayList<>();
        for (AccessContextResult candidate : available) {
            Id assignmentId = ids.convertIdFrom(candidate.assignmentId());
            SellerAuthenticationResult resolved = resolve(query.userId(), assignmentId);
            if (resolved != null) {
                valid.add(resolved);
            }
            if (valid.size() > 1) {
                break;
            }
        }
        if (valid.size() == 1) {
            return valid.getFirst();
        }
        return resolve(query.userId(), null);
    }

    private SellerAuthenticationResult resolve(Id userId, Id assignmentId) {
        Optional<AuthenticatedActor> found = identities.resolve(userId, assignmentId);
        if (found.isEmpty()) {
            return null;
        }
        AuthenticatedActor actor = found.orElseThrow();
        AccessContext context = actor.accessContext();
        if (context == null) {
            return new SellerAuthenticationResult(actor.platformUserId(), actor.email(), "ACTIVE",
                    actor.authenticationVersion(), null, null, null, actor.roles(), actor.authorities(), 0, 0);
        }
        Id scopeId = ids.convertIdFrom(context.scopeId());
        Optional<ScopeRevision> state = scopes.find(context.scopeKey(), scopeId);
        if (state.isEmpty()) {
            return null;
        }
        ScopeRevision revision = state.orElseThrow();
        return new SellerAuthenticationResult(actor.platformUserId(), actor.email(), "ACTIVE",
                actor.authenticationVersion(), context.assignmentId(), context.scopeKey(), context.scopeId(),
                actor.roles(), actor.authorities(), revision.merchantRevision(), revision.storefrontRevision());
    }
}
