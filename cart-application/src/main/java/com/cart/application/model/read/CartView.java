package com.cart.application.model.read;

import com.cart.domain.enums.CartStatus;

import java.util.List;

public record CartView(
        String cartId,
        String guestToken,
        String salesChannelId,
        String channelType,
        String regionId,
        String currencyCode,
        CartStatus status,
        List<CartLineView> lines
) {
}
