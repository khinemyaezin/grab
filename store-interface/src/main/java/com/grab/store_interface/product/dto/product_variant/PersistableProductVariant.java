package com.grab.store_interface.product.dto.product_variant;

import com.grab.store_interface.product.dto.product_varaiant_option.PersistableProductVariantOption;

import java.io.Serializable;
import java.util.List;

public record PersistableProductVariant(
        String productId,
        String sku,
        List<PersistableProductVariantOption> productVariantOptions
) implements Serializable {
}