package com.inventory.adapter.persistence.mapper.jpa;

import com.inventory.domain.aggregate.Bin;
import com.inventory.adapter.persistence.entity.BinEntity;

public interface BinJpaAssembler {
    BinEntity toEntity(Bin bin, BinEntity entity);
    Bin toDomain(BinEntity entity);
}
