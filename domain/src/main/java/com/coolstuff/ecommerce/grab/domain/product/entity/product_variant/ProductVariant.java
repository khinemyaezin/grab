package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.ProductVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.ReadableProductVariantOption;

import java.util.Set;

@Getter
@Setter
public class ProductVariant {
    private Long id;

    private String sku;

    private Set<ProductVariantOption> productVariantOptions;
}
