package com.grab.store_interface.product.dto.product_variant;

import com.grab.store_interface.product.dto.product_varaiant_option.ReadableProductVariantOption;

import java.io.Serializable;
import java.util.List;

public record ReadableProductVariant(
        String uuid,
        String sku,
        List<ReadableProductVariantOption> productVariantOptions) implements Serializable {
}
