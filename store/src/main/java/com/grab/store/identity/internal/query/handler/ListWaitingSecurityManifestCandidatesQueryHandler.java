package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.read.WaitingSecurityManifestView;
import com.identity.application.port.inbound.ListWaitingSecurityManifestCandidatesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class ListWaitingSecurityManifestCandidatesQueryHandler
        implements QueryHandler<ListWaitingSecurityManifestCandidatesQuery, List<WaitingSecurityManifestView>> {
    private final ListWaitingSecurityManifestCandidatesUseCase useCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public List<WaitingSecurityManifestView> handle(ListWaitingSecurityManifestCandidatesQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListWaitingSecurityManifestCandidatesQuery> getQueryType() {
        return ListWaitingSecurityManifestCandidatesQuery.class;
    }
}
