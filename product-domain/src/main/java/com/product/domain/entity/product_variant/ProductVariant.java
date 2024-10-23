package com.product.domain.entity.product_variant;

import com.product.domain.entity.product_variant_option.ProductVariantOption;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProductVariant {
    private String uuid;
    private String sku;
    private Set<ProductVariantOption> productVariantOptions;
}
