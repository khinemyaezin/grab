package com.grab.app.account.identity.internal.query.handler;

import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.identity.application.model.read.GetRoleDeclarationStatusQuery;
import com.identity.application.model.read.RoleDeclarationStatusView;
import com.identity.application.port.inbound.GetRoleDeclarationStatusUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetRoleDeclarationStatusQueryHandler
        implements QueryHandler<GetRoleDeclarationStatusQuery, RoleDeclarationStatusView> {

    private final GetRoleDeclarationStatusUseCase useCase;

    public GetRoleDeclarationStatusQueryHandler(GetRoleDeclarationStatusUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityReadTransactional
    public RoleDeclarationStatusView handle(GetRoleDeclarationStatusQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<GetRoleDeclarationStatusQuery> getQueryType() {
        return GetRoleDeclarationStatusQuery.class;
    }
}
