package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.ReadableProductVariantOption;

import java.util.Set;

@Getter
@Setter
public class ReadableProductVariant {
    private Long id;

    private String sku;

    private Set<ReadableProductVariantOption> productVariantOptions;
}
