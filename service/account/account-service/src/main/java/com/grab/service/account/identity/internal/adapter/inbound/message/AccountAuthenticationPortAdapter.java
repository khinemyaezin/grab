package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ResolveActiveSubjectPortMapper;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.VerifyAccountCredentialsPortMapper;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort;
import com.identity.application.model.read.ResolveActiveSubjectQuery;
import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AccountAuthenticationPortAdapter implements AccountAuthenticationPort {
    private final QueryBus queries;
    private final VerifyAccountCredentialsPortMapper credentials;
    private final ResolveActiveSubjectPortMapper state;

    public AccountAuthenticationPortAdapter(@Qualifier("accountQueryBus") QueryBus queries,
                                           VerifyAccountCredentialsPortMapper credentials,
                                           ResolveActiveSubjectPortMapper state) {
        this.queries = queries;
        this.credentials = credentials;
        this.state = state;
    }

    @Override
    public Subject verify(Credentials message) {
        VerifySellerCredentialsQuery query = credentials.toQuery(message);
        VerifiedSeller result = queries.dispatch(query);
        return credentials.toResponse(result);
    }

    @Override
    public Subject resolve(SubjectRequest message) {
        ResolveActiveSubjectQuery query = state.toQuery(message);
        ActiveSubjectView result = queries.dispatch(query);
        return state.toResponse(result);
    }
}
