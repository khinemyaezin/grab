package com.grab.service.contract.message.checkout.order;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Order Placement and Lifecycle mutations.
 */
public interface OrderPlacementPort extends Remote {

    OrderResult placeOrderFromCart(PlaceOrderMessage message) throws RemoteException;

    void cancelOrder(Id orderId, String reason) throws RemoteException;

    void updateOrderStatus(Id orderId, String newStatus) throws RemoteException;

    record PlaceOrderMessage(
            Id cartId,
            Id customerId,
            Id shippingAddressId,
            Id billingAddressId,
            String paymentMethodToken
    ) implements Serializable {}

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
    ) implements Serializable {}

    record OrderLineItemResult(
            Id id,
            Id variantId,
            String sku,
            String title,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) implements Serializable {}
}

