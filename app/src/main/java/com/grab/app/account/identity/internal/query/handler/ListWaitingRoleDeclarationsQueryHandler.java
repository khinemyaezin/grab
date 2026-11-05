package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.ListWaitingRoleDeclarationsQuery;
import com.identity.application.model.read.WaitingRoleDeclarationView;
import com.identity.application.port.inbound.ListWaitingRoleDeclarationsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListWaitingRoleDeclarationsQueryHandler
        implements QueryHandler<ListWaitingRoleDeclarationsQuery, List<WaitingRoleDeclarationView>> {

    private final ListWaitingRoleDeclarationsUseCase useCase;

    public ListWaitingRoleDeclarationsQueryHandler(ListWaitingRoleDeclarationsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public List<WaitingRoleDeclarationView> handle(ListWaitingRoleDeclarationsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ListWaitingRoleDeclarationsQuery> getQueryType() {
        return ListWaitingRoleDeclarationsQuery.class;
    }
}
