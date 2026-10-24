package com.inventory.application.policy;

import com.inventory.application.model.write.ProductVariantProjection;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;

public class ProductVariantProjectionPolicy {
    public boolean canRename(String productName) {
        return productName != null;
    }

    public ProductVariantProjection upsert(UpsertVariant update, ProductVariantProjection previous) {
        boolean replacePresentation = previous == null || update.replacePresentation();
        String productName = replacePresentation ? update.productName() : previous.productName();
        String status = replacePresentation ? "ACTIVE" : previous.status();
        return new ProductVariantProjection(
                update.productId(), update.variantId(), update.sku(), productName, update.manageInventory(), status
        );
    }

    public ProductVariantProjection status(ProductVariantProjection previous, boolean active) {
        String status = active ? "ACTIVE" : "DELETED";
        return new ProductVariantProjection(
                previous.productId(), previous.variantId(), previous.sku(), previous.productName(),
                previous.manageInventory(), status
        );
    }

    public ProductVariantProjection rename(ProductVariantProjection previous, String productName) {
        return new ProductVariantProjection(
                previous.productId(), previous.variantId(), previous.sku(), productName,
                previous.manageInventory(), previous.status()
        );
    }
}
