package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.StockMovementEntityMapper;
import com.inventory.infrastructure.mapper.jpa.StockMovementJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.StockMovementMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StockMovementJpaAssemblerImpl implements StockMovementJpaAssembler {
    private final StockMovementEntityMapper stockMovementEntityMapper;
    private final StockMovementMapper stockMovementMapper;

    @Override
    public StockMovementEntity buildFullEntityGraph(StockMovement movement, StockMovementEntity entity) {
        if (entity == null) {
            entity = new StockMovementEntity();
        }
        stockMovementEntityMapper.toEntity(movement, entity);
        return entity;
    }

    @Override
    public StockMovement toFullDomainGraph(StockMovementEntity stockMovementEntity) {
        return stockMovementMapper.toDomain(stockMovementEntity);
    }
}
