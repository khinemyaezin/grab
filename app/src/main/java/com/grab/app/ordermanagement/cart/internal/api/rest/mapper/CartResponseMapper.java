package com.grab.app.ordermanagement.cart.internal.api.rest.mapper;

import com.cart.application.model.read.CartResult;
import com.grab.app.ordermanagement.cart.internal.api.rest.dto.response.CartResponse;

public final class CartResponseMapper {

    private CartResponseMapper() {}

    public static CartResponse toResponse(CartResult result) {
        if (result == null) {
            return null;
        }
        return new CartResponse(
                result.cartId(),
                result.guestToken(),
                result.salesChannelId(),
                result.channelType(),
                result.regionId(),
                result.currencyCode(),
                result.status(),
                result.lines() == null ? null : result.lines().stream()
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
