package com.cart.domain.readmodel;

import java.math.BigDecimal;

public record CartLineView(
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
