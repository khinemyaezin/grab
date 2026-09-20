package com.grab.store.cart.internal.command;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.entity.CartLine;

import java.math.BigDecimal;
import java.util.List;

public record CartResult(
        String cartId,
        String guestToken,
        String salesChannelId,
        String channelType,
        String regionId,
        String currencyCode,
        String status,
        List<CartLineResult> lines,
        boolean priceAdjusted
) {
    public record CartLineResult(
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

    public static CartResult from(Cart cart, boolean priceAdjusted) {
        return new CartResult(
                cart.getId().getValue(),
                cart.getGuestToken(),
                cart.getSalesChannelId().getValue(),
                cart.getChannelType(),
                cart.getRegionId() == null ? null : cart.getRegionId().getValue(),
                cart.getCurrencyCode(),
                cart.getStatus().name(),
                cart.getLines().stream().map(CartResult::toLine).toList(),
                priceAdjusted
        );
    }

    private static CartLineResult toLine(CartLine line) {
        return new CartLineResult(
                line.getId().getValue(),
                line.getVariantId().getValue(),
                line.getProductId() == null ? null : line.getProductId().getValue(),
                line.getSellerId().getValue(),
                line.getTitle(),
                line.getSku(),
                line.getUnitPrice(),
                line.getQuantity()
        );
    }
}
