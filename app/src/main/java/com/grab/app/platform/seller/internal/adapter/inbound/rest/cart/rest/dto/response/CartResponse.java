package com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String cartId,
        String guestToken,
        String salesChannelId,
        String channelType,
        String regionId,
        String currencyCode,
        String status,
        List<CartLineResponse> lines,
        boolean priceAdjusted
) {
    public record CartLineResponse(
            String lineId,
            String variantId,
            String productId,
            String sellerId,
            String title,
            String sku,
            BigDecimal unitPrice,
            int quantity
    ) {
    }
}
