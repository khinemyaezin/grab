package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.RoleSearchResponse;
import com.grab.service.sellerbff.internal.query.model.SellerIdentityAdminQueries.SearchRolesQuery;
import com.grab.service.sellerbff.port.outbound.SellerIdentityAdminOutboundPort;
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

