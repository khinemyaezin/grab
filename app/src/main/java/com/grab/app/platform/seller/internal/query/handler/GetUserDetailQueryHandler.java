package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UserProfileDetailResponse;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.GetUserDetailQuery;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetUserDetailQueryHandler implements QueryHandler<GetUserDetailQuery, Optional<UserProfileDetailResponse>> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public GetUserDetailQueryHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<UserProfileDetailResponse> handle(GetUserDetailQuery query) {
        return outboundPort.findUserById(query.userId());
    }

    @Override
    public Class<GetUserDetailQuery> getQueryType() {
        return GetUserDetailQuery.class;
    }
}

