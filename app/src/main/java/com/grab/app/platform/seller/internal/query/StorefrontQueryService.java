package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.StorefrontQueryRequestMapper;
import com.merchant.application.model.write.StorefrontResult;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class StorefrontQueryService {
    private final QueryBus queries;
    private final StorefrontQueryRequestMapper mapper;

    public StorefrontResponse get(String storefrontId, String merchantId) {
        StorefrontResult result = queries.dispatch(mapper.toGetQuery(storefrontId, merchantId));
        return mapper.toResponse(result);
    }

    public List<StorefrontResponse> list(String merchantId) {
        List<StorefrontResult> results = queries.dispatch(mapper.toListQuery(merchantId));
        return mapper.toResponse(results);
    }
}
