package com.grab.app.checkout.cart.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Cart and Line Item management.
 * Replaces legacy CartController and CartRootController.
 */
public interface CartManagementPort {

    CartResult getOrCreateCart(GetOrCreateCartMessage message);

    CartResult addOrUpdateLineItem(Id cartId, AddOrUpdateLineItemMessage message);

    CartResult removeLineItem(Id cartId, Id lineItemId);

    void clearCart(Id cartId);

    Optional<CartResult> findCartById(Id cartId);

    record GetOrCreateCartMessage(
            Id customerId,
            String sessionId,
            Id salesChannelId,
            Id regionId,
            String currencyCode
    ) {}

    record AddOrUpdateLineItemMessage(
            Id variantId,
            int quantity
    ) {}

    record CartResult(
            Id id,
            Id customerId,
            String sessionId,
            Id salesChannelId,
            Id regionId,
            String currencyCode,
            List<LineItemResult> items,
            BigDecimal subtotal,
            BigDecimal estimatedTax,
            BigDecimal total,
            Instant updatedAt
    ) {}

    record LineItemResult(
            Id id,
            Id variantId,
            String sku,
            String title,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}

