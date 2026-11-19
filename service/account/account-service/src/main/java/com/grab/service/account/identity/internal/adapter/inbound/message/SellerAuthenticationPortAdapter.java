package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.VerifySellerCredentialsPortMapper;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthenticationPortAdapter implements SellerAuthenticationPort {
    private final QueryBus queries;
    private final VerifySellerCredentialsPortMapper credentials;

    public SellerAuthenticationPortAdapter(@Qualifier("accountQueryBus") QueryBus queries,
                                           VerifySellerCredentialsPortMapper credentials) {
        this.queries = queries;
        this.credentials = credentials;
    }

    @Override
    public VerifiedUser verify(Credentials message) {
        VerifySellerCredentialsQuery query = credentials.toQuery(message);
        VerifiedSeller result = queries.dispatch(query);
        return credentials.toResponse(result);
    }

}
