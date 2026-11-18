package com.grab.service.account.identity.internal.adapter.outbound.persistence;

import com.grab.framework.id.Id;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.port.inbound.ListAccessContextsUseCase;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.SellerIdentityStateReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SellerIdentityStateReadAdapter implements SellerIdentityStateReader {
    private final IdentityLookupQueryPort identities;
    private final ListAccessContextsUseCase contexts;

    public SellerIdentityStateReadAdapter(IdentityLookupQueryPort identities, ListAccessContextsUseCase contexts) {
        this.identities = identities;
        this.contexts = contexts;
    }

    @Override
    @IdentityReadTransactional
    public Optional<AuthenticatedActor> resolve(Id userId, Id assignmentId) {
        return identities.resolveSeller(userId, assignmentId);
    }

    @Override
    @IdentityReadTransactional
    public List<AccessContextResult> listContexts(Id userId) {
        ListAccessContextsQuery query = new ListAccessContextsQuery(userId);
        return contexts.execute(query);
    }
}
