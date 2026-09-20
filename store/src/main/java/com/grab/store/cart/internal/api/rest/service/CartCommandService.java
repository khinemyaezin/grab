package com.grab.store.cart.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.cart.internal.api.rest.dto.request.AddItemToCartRequest;
import com.grab.store.cart.internal.api.rest.dto.response.CartResponse;
import com.grab.store.cart.internal.command.AddItemToCartCommand;
import com.grab.store.cart.internal.command.CartResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartCommandService {
    private final CommandBus commandBus;

    public CartResponse addItem(String guestToken, AddItemToCartRequest request) {
        return toResponse(commandBus.dispatch(new AddItemToCartCommand(
                guestToken,
                request.salesChannelId(),
                request.regionId(),
                request.currencyCode(),
                request.variantId(),
                request.quantity(),
                request.displayedAmount()
        )));
    }

    static CartResponse toResponse(CartResult result) {
        return new CartResponse(
                result.cartId(),
                result.guestToken(),
                result.salesChannelId(),
                result.channelType(),
                result.regionId(),
                result.currencyCode(),
                result.status(),
                result.lines().stream()
                        .map(line -> new CartResponse.CartLineResponse(
                                line.lineId(),
                                line.variantId(),
                                line.productId(),
                                line.sellerId(),
                                line.title(),
                                line.sku(),
                                line.unitPrice(),
                                line.quantity()
                        ))
                        .toList(),
                result.priceAdjusted()
        );
    }
}
