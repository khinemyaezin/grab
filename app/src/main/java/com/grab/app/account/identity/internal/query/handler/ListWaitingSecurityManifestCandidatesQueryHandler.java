package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.read.WaitingSecurityManifestView;
import com.identity.application.port.inbound.ListWaitingSecurityManifestCandidatesUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListWaitingSecurityManifestCandidatesQueryHandler
        implements QueryHandler<ListWaitingSecurityManifestCandidatesQuery, List<WaitingSecurityManifestView>> {

    private final ListWaitingSecurityManifestCandidatesUseCase useCase;

    public ListWaitingSecurityManifestCandidatesQueryHandler(ListWaitingSecurityManifestCandidatesUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public List<WaitingSecurityManifestView> handle(ListWaitingSecurityManifestCandidatesQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListWaitingSecurityManifestCandidatesQuery> getQueryType() {
        return ListWaitingSecurityManifestCandidatesQuery.class;
    }
}
