package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.ReadableVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.VariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_type.ReadableVariantType;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_type.VariantType;

@Getter
@Setter
public class ProductVariantOption {
    // for mapping purpose
    private VariantType variantType;

    private String variantTypeValue;

    private VariantOption variantOption;

    private String variantOptionValue;
}
