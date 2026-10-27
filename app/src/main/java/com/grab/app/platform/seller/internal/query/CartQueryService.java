package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.response.CartResponse;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.mapper.CartResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartQueryService {
    private final QueryBus queryBus;

    public CartResponse current(String guestToken, String salesChannelId, String regionId) {
        return CartResponseMapper.toResponse(queryBus.dispatch(
                new GetCurrentCartQuery(guestToken, salesChannelId, regionId)
        ));
    }
}
