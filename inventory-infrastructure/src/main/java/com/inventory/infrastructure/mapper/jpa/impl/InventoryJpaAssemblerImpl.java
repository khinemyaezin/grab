package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.*;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class InventoryJpaAssemblerImpl implements InventoryJpaAssembler {
    private final InventoryItemEntityMapper inventoryItemEntityMapper;
    private final StockMovementEntityMapper stockMovementEntityMapper;
    private final StockMovementMapper stockMovementMapper;
    private final InventoryItemMapper inventoryItemMapper;

    @Override
    public InventoryItemEntity buildFullEntityGraph(InventoryItem inventoryItem, InventoryItemEntity entity) {
        if (entity == null) {
            entity = toInventoryItemEntity(inventoryItem);
            for (StockMovement movement : inventoryItem.getMovements()) {
                StockMovementEntity movementEntity = toStockMovementEntity(movement);
                entity.addMovement(movementEntity);
            }
        } else {
            mergeInventoryItemEntity(entity, inventoryItem);
            mergeMovements(entity, inventoryItem.getMovements());
        }
        return entity;
    }

    private InventoryItemEntity toInventoryItemEntity(InventoryItem inventoryItem) {
        InventoryItemEntity entity = new InventoryItemEntity();
        inventoryItemEntityMapper.toEntity(inventoryItem, entity);
        return entity;
    }

    private void mergeInventoryItemEntity(InventoryItemEntity entity, InventoryItem inventoryItem) {
        inventoryItemEntityMapper.toEntity(inventoryItem, entity);
    }

    private StockMovementEntity toStockMovementEntity(StockMovement movement) {
        StockMovementEntity entity = new StockMovementEntity();
        stockMovementEntityMapper.toEntity(movement, entity);
        return entity;
    }


    private void mergeMovements(InventoryItemEntity inventoryItemEntity, List<StockMovement> domainMovements) {
        Map<String, StockMovementEntity> existingByUuid = inventoryItemEntity.getMovements().stream()
                .collect(Collectors.toMap(StockMovementEntity::getUuid, Function.identity()));

        Set<String> processedUuids = new HashSet<>();
        List<StockMovementEntity> resultMovements = new ArrayList<>();

        for (StockMovement movementDomain : domainMovements) {
            String uuid = movementDomain.getId().getValue();
            StockMovementEntity movementEntity = existingByUuid.get(uuid);

            if (movementEntity != null) {
                processedUuids.add(uuid);
                resultMovements.add(movementEntity);
            } else {
                StockMovementEntity newMovementEntity = toStockMovementEntity(movementDomain);
                resultMovements.add(newMovementEntity);
            }
        }

        inventoryItemEntity.getMovements().removeIf(e -> !processedUuids.contains(e.getUuid()));

        resultMovements.stream()
                .filter(e -> !existingByUuid.containsKey(e.getUuid()))
                .forEach(inventoryItemEntity::addMovement);
    }

    @Override
    public InventoryItem toFullDomainGraph(InventoryItemEntity inventoryItemEntity) {
        List<StockMovement> stockMovements = new ArrayList<>(inventoryItemEntity.getMovements().size());
        for (StockMovementEntity movementEntity : inventoryItemEntity.getMovements()) {
            stockMovements.add(stockMovementMapper.toDomain(movementEntity));
        }
        return inventoryItemMapper.toDomain(inventoryItemEntity, stockMovements);
    }
}
