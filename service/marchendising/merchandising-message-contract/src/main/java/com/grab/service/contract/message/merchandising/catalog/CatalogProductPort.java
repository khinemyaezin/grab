package com.grab.service.contract.message.merchandising.catalog;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Product capabilities.
 * Replaces the legacy Product REST Controller and internal product service data exchange.
 */
public interface CatalogProductPort extends Remote {

    ProductResult createProduct(CreateProductMessage message) throws RemoteException;

    ProductResult updateProduct(Id merchantId, Id productId, UpdateProductMessage message) throws RemoteException;

    void deleteProduct(Id merchantId, Id productId) throws RemoteException;

    ProductResult updateProductStatus(Id merchantId, Id productId, String status) throws RemoteException;

    ProductResult findProductById(Id merchantId, Id productId) throws RemoteException;

    ProductResult findProductBySlug(String slug) throws RemoteException;

    List<ProductSummaryResult> searchProducts(ProductSearchCriteria criteria) throws RemoteException;

    VariantResult findVariantById(Id merchantId, Id productId, Id variantId) throws RemoteException;

    record CreateProductMessage(
            Id merchantId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            String status,
            List<CreateVariantMessage> variants
    ) implements Serializable {}

    record CreateVariantMessage(
            String sku,
            boolean manageInventory
    ) implements Serializable {}

    record UpdateProductMessage(
            String name,
            Id categoryId,
            String condition,
            String slug
    ) implements Serializable {}

    record ProductSearchCriteria(
            Id merchantId,
            String query,
            String variantStatus,
            Id categoryId,
            String productStatus,
            int page,
            int size
    ) implements Serializable {}

    record ProductResult(
            String id,
            String name,
            String categoryId,
            String categoryName,
            String condition,
            String status,
            String slug,
            List<VariantResult> variants
    ) implements Serializable {}

    record VariantResult(
            String id,
            String sku,
            String status,
            String matrixKey,
            boolean manageInventory
    ) implements Serializable {}

    record ProductSummaryResult(
            String productId,
            String productName,
            String status,
            String slug,
            String categoryName,
            String categoryId
    ) implements Serializable {}
}
