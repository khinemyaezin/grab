package com.grab.service.contract.message.channel.discovery;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;

/**
 * Universal Java Port Interface for Contextual Storefront Discovery and Product Search.
 * Replaces StorefrontProductController and StorefrontQueryRootController.
 */
public interface StorefrontDiscoveryPort extends Remote {

    List<StorefrontOfferMessageResult> searchProducts(StorefrontSearchCriteria criteria) throws RemoteException;

    StorefrontOfferMessageResult findProductBySlug(String publishableKey, String countryCode, String locale, String slug) throws RemoteException;

    ResolvedContextMessageResult resolveShoppingContext(String publishableKey, String countryCode, String locale) throws RemoteException;

    record StorefrontSearchCriteria(
            String publishableKey,
            String countryCode,
            String locale,
            String keyword,
            Id categoryId,
            int page,
            int size
    ) implements Serializable {}

    record ResolvedContextMessageResult(
            Id salesChannelId,
            Id regionId,
            String countryCode,
            String currencyCode,
            String locale,
            boolean regionServed
    ) implements Serializable {}

    record StorefrontOfferMessageResult(
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String productStatus,
            boolean published,
            boolean hasStock,
            boolean buyable,
            String minPrice,
            String maxPrice,
            List<VariantOfferMessageResult> variants
    ) implements Serializable {}

    record VariantOfferMessageResult(
            Id variantId,
            String sku,
            String title,
            String status,
            Map<String, String> optionValues,
            int availableQty,
            boolean inStock,
            boolean buyable,
            String amount,
            String originalAmount
    ) implements Serializable {}
}
