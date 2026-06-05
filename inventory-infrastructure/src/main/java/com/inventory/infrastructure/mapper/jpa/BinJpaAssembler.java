package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.Bin;
import com.inventory.infrastructure.entity.BinEntity;

public interface BinJpaAssembler {
    BinEntity toEntity(Bin bin, BinEntity entity);
    Bin toDomain(BinEntity entity);
}
