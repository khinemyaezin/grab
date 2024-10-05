package com.coolstuff.ecommerce.grab.domain.product.entity.product_variant;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MultiplePersistableProductVariants {
    private String productId;
    private Set<PersistableProductVariant> variants;
}
