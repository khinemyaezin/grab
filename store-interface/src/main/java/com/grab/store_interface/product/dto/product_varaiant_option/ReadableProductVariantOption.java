package com.grab.store_interface.product.dto.product_varaiant_option;

import com.grab.store_interface.product.dto.variant_option.ReadableVariantOption;
import com.grab.store_interface.product.dto.variant_type.ReadableVariantType;

public record ReadableProductVariantOption(String uuid,
                                           // for mapping purpose
                                           ReadableVariantType variantType,
                                           String variantTypeValue,
                                           ReadableVariantOption variantOption,
                                           String variantOptionValue) {
}
