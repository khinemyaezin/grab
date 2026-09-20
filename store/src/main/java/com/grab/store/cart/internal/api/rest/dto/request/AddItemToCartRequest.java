package com.grab.store.cart.internal.api.rest.dto.request;

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
