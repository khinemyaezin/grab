package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.query.model.SellerIdentityAdminQueries.ListAvailableAccessContextsQuery;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerAccessContextQueryService {

    private final QueryBus queryBus;

    public SellerAccessContextQueryService(@Qualifier("sellerQueryBus") QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    public List<AccessContextResponse> listContexts(SellerSecurityPrincipal principal) {
        String userId = principal != null ? principal.getPlatformUserId() : null;
        return queryBus.dispatch(new ListAvailableAccessContextsQuery(userId));
    }
}

