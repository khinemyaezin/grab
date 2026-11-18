package com.identity.application.service;

import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.ResolveActiveSubjectQuery;
import com.identity.application.port.inbound.ResolveActiveSubjectUseCase;
import com.identity.application.port.outbound.IdentityLookupQueryPort;

public final class ResolveActiveSubjectService implements ResolveActiveSubjectUseCase {
    private final IdentityLookupQueryPort identities;

    public ResolveActiveSubjectService(IdentityLookupQueryPort identities) {
        this.identities = identities;
    }

    @Override
    public ActiveSubjectView execute(ResolveActiveSubjectQuery query) {
        return identities.findActiveSubject(query.userId()).orElse(null);
    }
}
