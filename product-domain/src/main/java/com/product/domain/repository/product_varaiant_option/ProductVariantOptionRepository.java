package com.product.domain.repository.product_varaiant_option;

import com.product.domain.entity.product_variant_option.ProductVariantOption;

public interface ProductVariantOptionRepository {
    ProductVariantOption save(ProductVariantOption productVariantOption);

    void delete(String uuid);

    ProductVariantOption findByUuid(String uuid);
}
