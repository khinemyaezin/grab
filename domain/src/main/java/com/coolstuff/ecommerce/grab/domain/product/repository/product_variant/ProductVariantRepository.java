package com.coolstuff.ecommerce.grab.domain.product.repository.product_variant;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.PersistableProductVariant;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ProductVariant;

public interface ProductVariantRepository {
    ProductVariant save(PersistableProductVariant persistableProductVariant);

    void delete(String uuid);

    ProductVariant findByUuid(String uuid);
}
