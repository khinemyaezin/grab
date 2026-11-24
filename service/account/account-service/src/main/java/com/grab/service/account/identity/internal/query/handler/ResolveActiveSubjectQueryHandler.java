package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ResolveActiveSubjectQuery;
import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.port.inbound.ResolveActiveSubjectUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolveActiveSubjectQueryHandler implements QueryHandler<ResolveActiveSubjectQuery, ActiveSubjectView> {
    private final ResolveActiveSubjectUseCase useCase;

    @Override
    @IdentityReadTransactional
    public ActiveSubjectView handle(ResolveActiveSubjectQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ResolveActiveSubjectQuery> getQueryType() {
        return ResolveActiveSubjectQuery.class;
    }
}
