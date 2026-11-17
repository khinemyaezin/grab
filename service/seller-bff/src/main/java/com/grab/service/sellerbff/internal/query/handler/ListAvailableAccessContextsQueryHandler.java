package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.service.sellerbff.internal.query.model.SellerIdentityAdminQueries.ListAvailableAccessContextsQuery;
import com.grab.service.sellerbff.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAvailableAccessContextsQueryHandler
        implements QueryHandler<ListAvailableAccessContextsQuery, List<AccessContextResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ListAvailableAccessContextsQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<AccessContextResponse> handle(ListAvailableAccessContextsQuery query) {
        return outboundPort.listAvailableContexts(query.userId());
    }

    @Override
    public Class<ListAvailableAccessContextsQuery> getQueryType() {
        return ListAvailableAccessContextsQuery.class;
    }
}

