package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationQueryRepository;
import com.inventory.infrastructure.view.InventoryReservationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultInventoryReservationQueryRepository implements InventoryReservationQueryRepository {

    private final InventoryReservationJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Page<InventoryReservationView> queryByInventoryItemId(String inventoryItemId, Pageable pageable) {
        return executor.query("InventoryReservation", () -> jpaRepository.findAllByInventoryItemUuid(
                inventoryItemId,
                pageable
        ));
    }

    @Override
    public Page<InventoryReservationView> queryActiveByOrderId(String orderId, Pageable pageable) {
        return executor.query("InventoryReservation", () ->
                jpaRepository.findAllByOrderIdAndStatus(orderId, InventoryReservationStatus.ACTIVE, pageable));
    }
}
