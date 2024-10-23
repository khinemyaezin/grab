package com.product.domain.entity.product_variant_option;

import com.product.domain.entity.variant_option.VariantOption;
import com.product.domain.entity.variant_type.VariantType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantOption {
    private String uuid;
    // for mapping purpose
    private VariantType variantType;

    private String variantTypeValue;

    private VariantOption variantOption;

    private String variantOptionValue;
}
