package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.Id;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.model.read.ResolveSellerAccessQuery;
import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAccessResult;
import com.identity.application.model.read.SellerAuthenticationResult;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import com.identity.application.port.inbound.ResolveSellerAccessUseCase;
import com.identity.application.port.inbound.ResolveSellerAuthenticationUseCase;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Optional;
import java.util.Set;

public final class ResolveSellerAccessService implements ResolveSellerAccessUseCase {
    private final IdentityLookupUseCase identities;
    private final ResolveSellerAuthenticationUseCase sellerAuthentication;
    private final IdGenerator ids;
    private final String trustedNativeIssuer;

    public ResolveSellerAccessService(IdentityLookupUseCase identities,
                                      ResolveSellerAuthenticationUseCase sellerAuthentication,
                                      IdGenerator ids, String trustedNativeIssuer) {
        this.identities = identities;
        this.sellerAuthentication = sellerAuthentication;
        this.ids = ids;
        this.trustedNativeIssuer = trustedNativeIssuer;
    }

    @Override
    public SellerAccessResult execute(ResolveSellerAccessQuery query) {
        Optional<AuthenticatedActor> identity;
        try {
            identity = trustedNativeIssuer != null && !trustedNativeIssuer.isBlank()
                    && trustedNativeIssuer.equals(query.issuer())
                    ? identities.resolveByPlatformUserId(query.issuer(), query.subject(), null)
                    : identities.resolveByExternalIdentity(query.issuer(), query.subject(), Set.of(), null);
        } catch (IdentityDomainValidationException exception) {
            return empty(SellerAccessResult.Status.INACTIVE_IDENTITY);
        }
        if (identity.isEmpty()) {
            return empty(SellerAccessResult.Status.UNKNOWN_IDENTITY);
        }

        AuthenticatedActor actor = identity.orElseThrow();
        boolean chooseSingle = query.intent() == ResolveSellerAccessQuery.Intent.INITIAL;
        if (chooseSingle && query.assignmentId() != null) {
            return empty(SellerAccessResult.Status.INVALID_CONTEXT);
        }
        if (query.intent() == ResolveSellerAccessQuery.Intent.REVALIDATE && query.assignmentId() == null) {
            return emptyState(actor, null);
        }

        Id assignment = chooseSingle ? null : query.assignmentId();
        Id userId = ids.convertIdFrom(actor.platformUserId());
        SellerAuthenticationResult state = sellerAuthentication.execute(new ResolveSellerAuthenticationQuery(
                userId, assignment, chooseSingle));
        if (state == null) {
            SellerAccessResult.Status status = query.intent() == ResolveSellerAccessQuery.Intent.SELECT
                    ? SellerAccessResult.Status.DENIED : SellerAccessResult.Status.INVALID_CONTEXT;
            return empty(status);
        }
        Id resolvedUserId = ids.convertIdFrom(state.userId());
        Id assignmentId = state.assignmentId() == null ? null : ids.convertIdFrom(state.assignmentId());
        Id scopeId = state.scopeId() == null ? null : ids.convertIdFrom(state.scopeId());
        return new SellerAccessResult(SellerAccessResult.Status.ACTIVE, resolvedUserId, state.email(),
                state.authenticationVersion(), assignmentId, state.scopeKey(), scopeId,
                state.roles(), state.authorities(), state.merchantRevision(), state.storefrontRevision());
    }

    private SellerAccessResult emptyState(AuthenticatedActor actor, Id assignmentId) {
        Id userId = ids.convertIdFrom(actor.platformUserId());
        return new SellerAccessResult(SellerAccessResult.Status.ACTIVE, userId, actor.email(),
                actor.authenticationVersion(), assignmentId, null, null, Set.of(), Set.of(), 0, 0);
    }

    private SellerAccessResult empty(SellerAccessResult.Status status) {
        return new SellerAccessResult(status, null, null, 0, null, null, null,
                Set.of(), Set.of(), 0, 0);
    }
}
