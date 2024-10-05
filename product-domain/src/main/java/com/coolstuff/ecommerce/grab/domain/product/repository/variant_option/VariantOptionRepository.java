package com.coolstuff.ecommerce.grab.domain.product.repository.variant_option;

import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.PersistableVariantOption;
import com.coolstuff.ecommerce.grab.domain.product.entity.variant_option.VariantOption;

public interface VariantOptionRepository {
    VariantOption save(PersistableVariantOption persistableVariantOption);

    void delete(String uuid);

    VariantOption findByUuid(String uuid);
}
