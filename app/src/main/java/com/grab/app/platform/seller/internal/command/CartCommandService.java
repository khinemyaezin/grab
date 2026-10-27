package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.request.AddItemToCartRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.response.CartResponse;
import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.mapper.CartResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartCommandService {
    private final CommandBus commandBus;

    public CartResponse addItem(String guestToken, AddItemToCartRequest request) {
        return CartResponseMapper.toResponse(commandBus.dispatch(new AddItemToCartCommand(
                guestToken,
                request.salesChannelId(),
                request.regionId(),
                request.currencyCode(),
                request.variantId(),
                request.quantity(),
                request.displayedAmount()
        )));
    }
}
