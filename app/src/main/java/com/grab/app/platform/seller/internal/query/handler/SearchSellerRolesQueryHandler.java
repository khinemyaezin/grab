package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RoleSearchResponse;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.SearchRolesQuery;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchSellerRolesQueryHandler implements QueryHandler<SearchRolesQuery, List<RoleSearchResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public SearchSellerRolesQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<RoleSearchResponse> handle(SearchRolesQuery query) {
        return outboundPort.searchRoles(query.name());
    }

    @Override
    public Class<SearchRolesQuery> getQueryType() {
        return SearchRolesQuery.class;
    }
}

