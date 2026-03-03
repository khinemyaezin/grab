package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.StockMovementEntityMapper;
import com.inventory.infrastructure.mapper.jpa.StockMovementMapper;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DefaultStockMovementRepository implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;
    private final StockMovementEntityMapper entityMapper;
    private final StockMovementMapper domainMapper;

    @Override
    public void save(StockMovement movement) {
        StockMovementEntity entity = new StockMovementEntity();
        entityMapper.toEntity(movement, entity);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<StockMovement> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue())
                .map(domainMapper::toDomain);
    }

    @Override
    public List<StockMovement> findByInventoryItemId(Id inventoryItemId) {
        return jpaRepository.findAllByInventoryItemUuid(inventoryItemId.getValue()).stream()
                .map(domainMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findByInventoryItemIdAndDateRange(Id inventoryItemId, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByInventoryItemUuidAndDateRange(inventoryItemId.getValue(), from, to).stream()
                .map(domainMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findByReferenceId(String referenceId) {
        return jpaRepository.findAllByReferenceId(referenceId).stream()
                .map(domainMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findByType(StockMovementType type) {
        return jpaRepository.findAllByType(type).stream()
                .map(domainMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findRecentMovements(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return jpaRepository.findRecentMovements(since).stream()
                .map(domainMapper::toDomain)
                .toList();
    }

    @Override
    public int countByInventoryItemIdAndType(Id inventoryItemId, StockMovementType type) {
        return jpaRepository.countByInventoryItemUuidAndType(inventoryItemId.getValue(), type);
    }
}
