package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.StockMovementJpaAssembler;
import com.inventory.infrastructure.repository.jpa.StockMovementJpaRepository;
import com.inventory.infrastructure.repository.jpa.StockMovementQueryRepository;
import com.inventory.infrastructure.view.StockMovementView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultStockMovementRepository implements StockMovementRepository, StockMovementQueryRepository {

    private static final Logger log = Loggers.getLogger(DefaultStockMovementRepository.class);

    private final StockMovementJpaRepository jpaRepository;
    private final StockMovementJpaAssembler mapper;
    private final PersistenceExecutor executor;

    @Override
    public void save(StockMovement movement) {
        executor.command("StockMovement", () -> {
            log.info("Persisting stock movement id={}, type={}", movement.getId().getValue(), movement.getType());
            Optional<StockMovementEntity> existingEntity = jpaRepository.findByUuid(movement.getId().getValue());
            StockMovementEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(movement, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(movement, null);
            }
            jpaRepository.save(entity);
            log.debug("Persisted stock movement id={}", movement.getId().getValue());
            return null;
        });
    }

    @Override
    public Optional<StockMovement> findById(Id id) {
        log.debug("Loading stock movement by id={}", id.getValue());
        return executor.query("StockMovement", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public Page<StockMovementView> queryByInventoryItemId(String inventoryItemId, Pageable pageable) {
        return executor.query("StockMovement", () ->
                jpaRepository.findAllByInventoryItemUuidOrderByCreatedAtDesc(
                        inventoryItemId,
                        pageable
                ));
    }

    @Override
    public Page<StockMovementView> queryByInventoryItemIdAndDateRange(String inventoryItemId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return executor.query("StockMovement", () ->
                jpaRepository.findByInventoryItemUuidAndDateRange(inventoryItemId, from, to, pageable));
    }

    @Override
    public Page<StockMovementView> queryByReferenceId(String referenceId, Pageable pageable) {
        return executor.query("StockMovement", () ->
                jpaRepository.findAllByReferenceId(referenceId, pageable));
    }

    @Override
    public Page<StockMovementView> queryByType(StockMovementType type, Pageable pageable) {
        return executor.query("StockMovement", () ->
                jpaRepository.findAllByType(type, pageable));
    }

    @Override
    public Page<StockMovementView> queryRecentMovements(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        log.debug("Loading recent stock movements since={} for days={}", since, days);
        return executor.query("StockMovement", () ->
                jpaRepository.findRecentMovements(since, pageable));
    }

    @Override
    public int countByInventoryItemIdAndType(String inventoryItemId, StockMovementType type) {
        return executor.query("StockMovement", () ->
                jpaRepository.countByInventoryItemUuidAndType(inventoryItemId, type));
    }
}
