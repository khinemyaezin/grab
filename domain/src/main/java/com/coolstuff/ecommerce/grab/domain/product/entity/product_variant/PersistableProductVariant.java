package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.PersistableProductVariantOption;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class PersistableProductVariant implements Serializable {
    private String sku;
    private Set<PersistableProductVariantOption> productVariantOptions = new HashSet<>();
}
