package com.product.domain.repository.variant_type;

import com.product.domain.entity.variant_type.VariantType;

public interface VariantTypeRepository {
    void delete(String uuid);

    VariantType findByUuid(String uuid);
}
