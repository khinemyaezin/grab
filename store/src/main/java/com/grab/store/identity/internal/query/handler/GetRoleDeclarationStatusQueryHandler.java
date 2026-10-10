package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.GetRoleDeclarationStatusQuery;
import com.identity.application.model.read.RoleDeclarationStatusView;
import com.identity.application.port.inbound.GetRoleDeclarationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class GetRoleDeclarationStatusQueryHandler
        implements QueryHandler<GetRoleDeclarationStatusQuery, RoleDeclarationStatusView> {
    private final GetRoleDeclarationStatusUseCase useCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public RoleDeclarationStatusView handle(GetRoleDeclarationStatusQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<GetRoleDeclarationStatusQuery> getQueryType() {
        return GetRoleDeclarationStatusQuery.class;
    }
}
