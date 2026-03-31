package com.catalog.domain.repository;

import com.catalog.domain.aggregate.VariantType;
import com.grab.framework.id.Id;

import java.util.Optional;

public interface VariantTypeRepository {
    Optional<VariantType> findById(Id typeId);
    void save(VariantType variantType);

}
