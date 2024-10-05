package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableProductVariantOption {
    // for mapping purpose
    private String variantTypeId; // color, size
    private String variantTypeValue;

    // for mapping purpose
    private String variantOptionId; // yellow or small
    private String variantOptionValue; // အဝါရောင်
}
