package com.product.domain.repository.variant_option;

import com.product.domain.entity.variant_option.VariantOption;

public interface VariantOptionRepository {
    VariantOption save(VariantOption variantOption);

    void delete(String uuid);

    VariantOption findByUuid(String uuid);
}
