package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ListWaitingRoleDeclarationsQuery;
import com.identity.application.model.read.WaitingRoleDeclarationView;
import com.identity.application.port.inbound.ListWaitingRoleDeclarationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListWaitingRoleDeclarationsQueryHandler
        implements QueryHandler<ListWaitingRoleDeclarationsQuery, List<WaitingRoleDeclarationView>> {
    private final ListWaitingRoleDeclarationsUseCase useCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public List<WaitingRoleDeclarationView> handle(ListWaitingRoleDeclarationsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListWaitingRoleDeclarationsQuery> getQueryType() {
        return ListWaitingRoleDeclarationsQuery.class;
    }
}
