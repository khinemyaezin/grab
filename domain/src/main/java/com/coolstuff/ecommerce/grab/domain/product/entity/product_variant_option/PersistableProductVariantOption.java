package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.VariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_type.VariantType;

@Getter
@Setter
public class PersistableProductVariantOption {
    // for mapping purpose
    private VariantType variantType; // color, size
    private String variantTypeValue;

    // for mapping purpose
    private VariantOption variantOption; // yellow or small
    private String variantOptionValue; // အဝါရောင်
}
