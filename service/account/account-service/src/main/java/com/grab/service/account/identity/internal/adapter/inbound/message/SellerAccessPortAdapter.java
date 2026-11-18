package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ResolveSellerAccessPortMapper;
import com.grab.service.contract.message.account.identity.SellerAccessPort;
import com.identity.application.model.read.ResolveSellerAccessQuery;
import com.identity.application.model.read.SellerAccessResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SellerAccessPortAdapter implements SellerAccessPort {
    private final QueryBus queries;
    private final ResolveSellerAccessPortMapper mapper;

    public SellerAccessPortAdapter(@Qualifier("accountQueryBus") QueryBus queries,
                                   ResolveSellerAccessPortMapper mapper) {
        this.queries = queries;
        this.mapper = mapper;
    }

    @Override
    public AccessState resolve(AccessRequest request) {
        ResolveSellerAccessQuery query = mapper.toQuery(request);
        SellerAccessResult result = queries.dispatch(query);
        return mapper.toResponse(result);
    }
}
