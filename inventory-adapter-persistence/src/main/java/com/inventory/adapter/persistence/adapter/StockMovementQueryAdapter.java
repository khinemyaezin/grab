package com.inventory.adapter.persistence.adapter;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.adapter.persistence.repository.jpa.StockMovementJpaRepository;
import com.inventory.application.port.outbound.StockMovementQueryPort;
import com.inventory.application.model.read.StockMovementView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class StockMovementQueryAdapter implements StockMovementQueryPort {

    private static final Logger log = Loggers.getLogger(StockMovementQueryAdapter.class);

    private final StockMovementJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

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
