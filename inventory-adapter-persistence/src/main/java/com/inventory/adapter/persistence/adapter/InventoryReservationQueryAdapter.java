package com.inventory.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.adapter.persistence.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.application.port.outbound.InventoryReservationQueryPort;
import com.inventory.application.model.read.InventoryReservationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class InventoryReservationQueryAdapter implements InventoryReservationQueryPort {

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
