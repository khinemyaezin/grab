package com.grab.app.checkout.cart.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Cart and Line Item management.
 * Replaces legacy CartController and CartRootController.
 */
public interface CartManagementPort {

    CartMessageResult getCurrentCart(GetCurrentCartMessage message);

    CartMessageResult addItem(AddItemMessage message);

    record GetCurrentCartMessage(
            String guestToken,
            String salesChannelId,
            String regionId
    ) {}

    record AddItemMessage(
            String guestToken,
            String salesChannelId,
            String regionId,
            String currencyCode,
            String variantId,
            int quantity,
            BigDecimal displayedAmount
    ) {}

    record CartMessageResult(
            String cartId,
            String guestToken,
            String salesChannelId,
            String channelType,
            String regionId,
            String currencyCode,
            String status,
            List<CartLineMessageResult> lines,
            boolean priceAdjusted
    ) {}

    record CartLineMessageResult(
            String lineId,
            String variantId,
            String productId,
            String sellerId,
            String title,
            String sku,
            BigDecimal unitPrice,
            int quantity
    ) {}
}
