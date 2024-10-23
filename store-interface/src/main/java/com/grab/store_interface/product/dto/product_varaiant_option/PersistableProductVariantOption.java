package com.grab.store_interface.product.dto.product_varaiant_option;

/**
 * @param variantTypeId      for mapping purpose color, size
 * @param variantOptionId    for mapping purpose yellow or small
 * @param variantOptionValue အဝါရောင်
 */
public record PersistableProductVariantOption(String variantTypeId,
                                              String variantTypeValue,
                                              String variantOptionId,
                                              String variantOptionValue) {
}
