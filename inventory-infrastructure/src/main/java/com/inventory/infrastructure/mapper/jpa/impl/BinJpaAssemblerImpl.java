package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.Bin;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.mapper.jpa.BinEntityMapper;
import com.inventory.infrastructure.mapper.jpa.BinJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.BinMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BinJpaAssemblerImpl implements BinJpaAssembler {
    private final BinEntityMapper binEntityMapper;
    private final BinMapper binMapper;

    @Override
    public BinEntity toEntity(Bin bin, BinEntity entity) {
        if (entity == null) {
            entity = new BinEntity();
        }
        binEntityMapper.toEntity(bin, entity);
        return entity;
    }

    @Override
    public Bin toDomain(BinEntity entity) {
        return binMapper.toDomain(entity);
    }
}
