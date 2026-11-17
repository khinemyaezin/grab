package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAuthenticationResult;
import com.identity.application.port.inbound.ResolveSellerAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class ResolveSellerAuthenticationQueryHandler implements QueryHandler<ResolveSellerAuthenticationQuery, SellerAuthenticationResult> {
    private final ResolveSellerAuthenticationUseCase useCase;

    @Override
    @IdentityReadTransactional(propagation = Propagation.NOT_SUPPORTED)
    public SellerAuthenticationResult handle(ResolveSellerAuthenticationQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<ResolveSellerAuthenticationQuery> getQueryType() {
        return ResolveSellerAuthenticationQuery.class;
    }
}
