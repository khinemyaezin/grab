package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.StockMovementJpaAssembler;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultStockMovementRepository implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;
    private final StockMovementJpaAssembler mapper;
    private final PersistenceExecutor executor;

    @Override
    public void save(StockMovement movement) {
        executor.command("StockMovement", () -> {
            Optional<StockMovementEntity> existingEntity = jpaRepository.findByUuid(movement.getId().getValue());
            StockMovementEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(movement, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(movement, null);
            }
            jpaRepository.save(entity);
            return null;
        });
    }

    @Override
    public Optional<StockMovement> findById(Id id) {
        return executor.query("StockMovement", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<StockMovement> findByInventoryItemId(Id inventoryItemId) {
        return executor.query("StockMovement", () -> jpaRepository.findAllByInventoryItemUuid(inventoryItemId.getValue()).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<StockMovement> findByInventoryItemIdAndDateRange(Id inventoryItemId, LocalDateTime from, LocalDateTime to) {
        return executor.query("StockMovement", () -> jpaRepository.findByInventoryItemUuidAndDateRange(inventoryItemId.getValue(), from, to).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<StockMovement> findByReferenceId(String referenceId) {
        return executor.query("StockMovement", () -> jpaRepository.findAllByReferenceId(referenceId).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<StockMovement> findByType(StockMovementType type) {
        return executor.query("StockMovement", () -> jpaRepository.findAllByType(type).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<StockMovement> findRecentMovements(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return executor.query("StockMovement", () -> jpaRepository.findRecentMovements(since).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public int countByInventoryItemIdAndType(Id inventoryItemId, StockMovementType type) {
        return executor.query("StockMovement", () -> jpaRepository.countByInventoryItemUuidAndType(inventoryItemId.getValue(), type));
    }
}
