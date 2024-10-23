package com.product.domain.entity.product_variant_option;

import com.product.domain.entity.variant_option.ReadableVariantOption;
import com.product.domain.entity.variant_type.ReadableVariantType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableProductVariantOption {
    private String uuid;
    // for mapping purpose
    private ReadableVariantType variantType;

    private String variantTypeValue;

    private ReadableVariantOption variantOption;

    private String variantOptionValue;
}
