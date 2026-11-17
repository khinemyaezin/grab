package com.grab.service.contract.message.merchandising.catalog;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;

/**
 * Universal Java Port Interface for Product Discovery and Buyability queries.
 * Unifies legacy CatalogDiscoveryQuery and CatalogBuyabilityQuery.
 */
public interface CatalogDiscoveryPort extends Remote {

    ProductFactsResult findProductFacts(Id productId) throws RemoteException;

    VariantFactsResult findVariantFacts(Id variantId) throws RemoteException;

    boolean isPublishedInChannel(Id productId, Id salesChannelId) throws RemoteException;

    CatalogVariantSliceResult findPublishedVariantSlice(Id variantId, Id salesChannelId) throws RemoteException;

    CatalogVariantSliceResult findVariant(Id variantId) throws RemoteException;

    List<Id> variantIdsForProduct(Id productId) throws RemoteException;

    List<PublicationSliceResult> listPublications() throws RemoteException;

    List<ProductChannelPairResult> findPublishedProductChannelsAfter(Id productId, Id salesChannelId, int limit) throws RemoteException;

    List<ProductChannelPairResult> findPublishedProductChannelsBySku(String sku) throws RemoteException;

    List<ProductChannelPairResult> findPublishedProductChannelsForSalesChannelAfter(Id salesChannelId, Id afterProductId, int limit) throws RemoteException;

    record ProductFactsResult(
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String status,
            List<ProductOptionResult> options,
            List<VariantFactsResult> variants
    ) implements Serializable {
        public ProductFactsResult {
            options = options == null ? List.of() : List.copyOf(options);
            variants = variants == null ? List.of() : List.copyOf(variants);
        }
    }

    record ProductOptionResult(String name, List<String> values) implements Serializable {
        public ProductOptionResult {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    record VariantFactsResult(
            Id productId,
            Id variantId,
            String sku,
            String title,
            String status,
            boolean manageInventory,
            Map<String, String> optionValues,
            List<Id> salesChannelIds
    ) implements Serializable {
        public VariantFactsResult {
            optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
            salesChannelIds = salesChannelIds == null ? List.of() : List.copyOf(salesChannelIds);
        }
    }

    record CatalogVariantSliceResult(
            Id variantId,
            Id productId,
            Id merchantId,
            String sku,
            String title,
            String slug,
            String productStatus,
            boolean untracked,
            String media
    ) implements Serializable {
        public boolean active() {
            return "ACTIVE".equalsIgnoreCase(productStatus);
        }
    }

    record PublicationSliceResult(Id variantId, Id salesChannelId) implements Serializable {}

    record ProductChannelPairResult(Id productId, Id salesChannelId) implements Serializable {}
}

