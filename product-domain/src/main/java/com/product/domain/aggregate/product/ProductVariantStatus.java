package com.product.domain.aggregate.product;

/**
 * Status of a product variant.
 * Used for soft delete to preserve variant history
 */
public enum ProductVariantStatus {
    /**
     * Variant is active and visible
     */
    ACTIVE,

    /**
     * Variant has been soft-deleted.
     * The variant remains in storage to prevent its combination
     * from being recreated when new variant types are added.
     */
    DELETED

}
