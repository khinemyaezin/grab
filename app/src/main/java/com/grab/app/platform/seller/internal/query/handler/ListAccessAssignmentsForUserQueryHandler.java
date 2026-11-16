package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.ListAccessAssignmentsForUserQuery;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAccessAssignmentsForUserQueryHandler
        implements QueryHandler<ListAccessAssignmentsForUserQuery, List<AccessAssignmentResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ListAccessAssignmentsForUserQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<AccessAssignmentResponse> handle(ListAccessAssignmentsForUserQuery query) {
        return outboundPort.listAssignmentsForUser(query.userId(), query.actorScopeKey(), query.actorScopeId());
    }

    @Override
    public Class<ListAccessAssignmentsForUserQuery> getQueryType() {
        return ListAccessAssignmentsForUserQuery.class;
    }
}

