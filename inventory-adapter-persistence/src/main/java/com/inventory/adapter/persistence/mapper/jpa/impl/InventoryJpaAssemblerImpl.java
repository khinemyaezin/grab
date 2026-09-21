package com.inventory.adapter.persistence.mapper.jpa.impl;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.adapter.persistence.entity.InventoryItemEntity;
import com.inventory.adapter.persistence.mapper.jpa.InventoryItemEntityMapper;
import com.inventory.adapter.persistence.mapper.jpa.InventoryItemMapper;
import com.inventory.adapter.persistence.mapper.jpa.InventoryJpaAssembler;
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
