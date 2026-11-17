package com.grab.service.account.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.account.identity.internal.config.IdentityReadTransactional;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.port.inbound.VerifySellerCredentialsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerifySellerCredentialsQueryHandler implements QueryHandler<VerifySellerCredentialsQuery, VerifiedSeller> {
    private final VerifySellerCredentialsUseCase useCase;

    @Override
    @IdentityReadTransactional
    public VerifiedSeller handle(VerifySellerCredentialsQuery query) {
        return useCase.execute(query);
    }

    @Override
    public Class<VerifySellerCredentialsQuery> getQueryType() {
        return VerifySellerCredentialsQuery.class;
    }
}
