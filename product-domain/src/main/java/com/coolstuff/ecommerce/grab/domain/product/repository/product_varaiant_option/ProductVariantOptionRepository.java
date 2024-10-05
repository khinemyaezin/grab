package com.coolstuff.ecommerce.grab.domain.product.repository.product_varaiant_option;

import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.PersistableProductVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant_option.ProductVariantOption;

public interface ProductVariantOptionRepository {
    ProductVariantOption save(PersistableProductVariantOption persistableProductVariantOption);

    void delete(String uuid);

    ProductVariantOption findByUuid(String uuid);
}
