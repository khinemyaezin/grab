package com.grab.service.contract.message.checkout.cart;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Cart and Line Item management.
 * Replaces legacy CartController and CartRootController.
 */
public interface CartManagementPort extends Remote {

    CartMessageResult getCurrentCart(GetCurrentCartMessage message) throws RemoteException;

    CartMessageResult addItem(AddItemMessage message) throws RemoteException;

    record GetCurrentCartMessage(
            String guestToken,
            String salesChannelId,
            String regionId
    ) implements Serializable {}

    record AddItemMessage(
            String guestToken,
            String salesChannelId,
            String regionId,
            String currencyCode,
            String variantId,
            int quantity,
            BigDecimal displayedAmount
    ) implements Serializable {}

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
    ) implements Serializable {}

    record CartLineMessageResult(
            String lineId,
            String variantId,
            String productId,
            String sellerId,
            String title,
            String sku,
            BigDecimal unitPrice,
            int quantity
    ) implements Serializable {}
}
