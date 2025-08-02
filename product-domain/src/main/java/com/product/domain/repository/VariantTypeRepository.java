package com.product.domain.repository;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;

import java.util.Optional;

public interface VariantTypeRepository {
    void save(VariantType product);
    Optional<VariantType> find(Id id);
    Optional<VariantType> findByVariantOption(Id id);
    Boolean exists(Id id);
    void delete(Id id);
}
