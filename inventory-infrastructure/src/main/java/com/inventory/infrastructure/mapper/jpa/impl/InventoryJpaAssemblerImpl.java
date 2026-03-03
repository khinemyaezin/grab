package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryItemEntityMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryItemMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryJpaAssembler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryJpaAssemblerImpl implements InventoryJpaAssembler {
    private final InventoryItemEntityMapper inventoryItemEntityMapper;
    private final InventoryItemMapper inventoryItemMapper;

    @Override
    public InventoryItemEntity buildFullEntityGraph(InventoryItem inventoryItem, InventoryItemEntity entity) {
        if (entity == null) {
            entity = new InventoryItemEntity();
        }
        inventoryItemEntityMapper.toEntity(inventoryItem, entity);
        return entity;
    }

    @Override
    public InventoryItem toFullDomainGraph(InventoryItemEntity inventoryItemEntity) {
        return inventoryItemMapper.toDomain(inventoryItemEntity);
    }
}
