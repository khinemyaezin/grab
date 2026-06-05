package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.view.InventoryReservationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservationEntity, Long> {
    Optional<InventoryReservationEntity> findByUuid(String uuid);
    Optional<InventoryReservationEntity> findByIdempotencyKey(String idempotencyKey);
    Page<InventoryReservationView> findAllByInventoryItemUuid(String inventoryItemUuid, Pageable pageable);
    Page<InventoryReservationView> findAllByOrderIdAndStatus(String orderId, InventoryReservationStatus status, Pageable pageable);
}
