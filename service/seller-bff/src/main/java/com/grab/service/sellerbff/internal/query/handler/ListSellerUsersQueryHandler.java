package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.query.model.SellerIdentityAdminQueries.ListUsersQuery;
import com.grab.service.sellerbff.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerUsersQueryHandler implements QueryHandler<ListUsersQuery, List<SellerUserProfileResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ListSellerUsersQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<SellerUserProfileResponse> handle(ListUsersQuery query) {
        return outboundPort.listUsers(query.page(), query.size());
    }

    @Override
    public Class<ListUsersQuery> getQueryType() {
        return ListUsersQuery.class;
    }
}

