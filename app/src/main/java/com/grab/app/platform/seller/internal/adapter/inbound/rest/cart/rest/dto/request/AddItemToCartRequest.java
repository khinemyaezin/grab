package com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.request;

import java.math.BigDecimal;

public record AddItemToCartRequest(
        String salesChannelId,
        String regionId,
        String currencyCode,
        String variantId,
        int quantity,
        BigDecimal displayedAmount
) {
}
