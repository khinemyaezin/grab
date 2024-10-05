package com.coolstuff.ecommerce.grab.domain.product.entity.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ReadableProductVariant;

import java.util.List;
import java.util.Map;

public class ReadableProductListing {
    private Map<String, ReadableProductVariant> variants;
    private Map<String, List<String>> options;
    private Map<String, String> variantOptionMapping;
}
