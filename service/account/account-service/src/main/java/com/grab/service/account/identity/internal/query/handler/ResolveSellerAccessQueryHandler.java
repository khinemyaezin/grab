package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ResolveSellerAccessQuery;
import com.identity.application.model.read.SellerAccessResult;
import com.identity.application.port.inbound.ResolveSellerAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class ResolveSellerAccessQueryHandler implements QueryHandler<ResolveSellerAccessQuery, SellerAccessResult> {
    private final ResolveSellerAccessUseCase useCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.NOT_SUPPORTED)
    public SellerAccessResult handle(ResolveSellerAccessQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ResolveSellerAccessQuery> getQueryType() {
        return ResolveSellerAccessQuery.class;
    }
}
