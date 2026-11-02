package com.grab.app.checkout.order.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Order Placement and Lifecycle mutations.
 */
public interface OrderPlacementPort {

    OrderResult placeOrderFromCart(PlaceOrderMessage message);

    void cancelOrder(Id orderId, String reason);

    void updateOrderStatus(Id orderId, String newStatus);

    record PlaceOrderMessage(
            Id cartId,
            Id customerId,
            Id shippingAddressId,
            Id billingAddressId,
            String paymentMethodToken
    ) {}

    record OrderResult(
            Id id,
            String orderNumber,
            Id customerId,
            Id salesChannelId,
            Id regionId,
            String currencyCode,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal totalAmount,
            String status, // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
            List<OrderLineItemResult> items,
            Instant createdAt
    ) {}

    record OrderLineItemResult(
            Id id,
            Id variantId,
            String sku,
            String title,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}

