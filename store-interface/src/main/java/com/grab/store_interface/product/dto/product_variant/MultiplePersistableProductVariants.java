package com.grab.store_interface.product.dto.product_variant;

import java.util.Set;

public record MultiplePersistableProductVariants(String productId, Set<PersistableProductVariant> variants) {
}
