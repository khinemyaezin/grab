package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.ReadableVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_type.ReadableVariantType;

@Getter
@Setter
public class ReadableProductVariantOption {
    // for mapping purpose
    private ReadableVariantType variantType;

    private String variantTypeValue;

    private ReadableVariantOption variantOption;

    private String variantOptionValue;
}
