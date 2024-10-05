package com.coolstuff.ecommerce.grab.domain.product.repository.variant_type;

import com.coolstuff.ecommerce.grab.domain.product.entity.variant_type.VariantType;

public interface VariantTypeRepository {
    void delete(String uuid);

    VariantType findByUuid(String uuid);
}
