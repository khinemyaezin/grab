package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.ListRolesQuery;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerRolesQueryHandler implements QueryHandler<ListRolesQuery, List<RoleResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ListSellerRolesQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<RoleResponse> handle(ListRolesQuery query) {
        return outboundPort.listRoles(query.name(), query.page(), query.size());
    }

    @Override
    public Class<ListRolesQuery> getQueryType() {
        return ListRolesQuery.class;
    }
}

