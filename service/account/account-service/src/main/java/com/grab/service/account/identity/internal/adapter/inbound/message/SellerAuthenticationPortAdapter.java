package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ResolveSellerAuthenticationPortMapper;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.VerifySellerCredentialsPortMapper;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAuthenticationResult;
import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthenticationPortAdapter implements SellerAuthenticationPort {
    private final QueryBus queries;
    private final VerifySellerCredentialsPortMapper credentials;
    private final ResolveSellerAuthenticationPortMapper state;

    public SellerAuthenticationPortAdapter(@Qualifier("accountQueryBus") QueryBus queries,
                                           VerifySellerCredentialsPortMapper credentials,
                                           ResolveSellerAuthenticationPortMapper state) {
        this.queries = queries;
        this.credentials = credentials;
        this.state = state;
    }

    @Override
    public VerifiedUser verify(Credentials message) {
        VerifySellerCredentialsQuery query = credentials.toQuery(message);
        VerifiedSeller result = queries.dispatch(query);
        return credentials.toResponse(result);
    }

    @Override
    public AuthenticationState resolve(StateRequest message) {
        ResolveSellerAuthenticationQuery query = state.toQuery(message);
        SellerAuthenticationResult result = queries.dispatch(query);
        return state.toResponse(result);
    }
}
