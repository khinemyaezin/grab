package com.coolstuff.ecommerce.grab.domain.product.repository.product_variant;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.PersistableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ReadableProductVariant;

public interface ProductVariantRepository {
    ReadableProductVariant save(PersistableProductVariant persistableProductVariant);

    void delete(String uuid);

    ReadableProductVariant findByUuid(String uuid);
}
