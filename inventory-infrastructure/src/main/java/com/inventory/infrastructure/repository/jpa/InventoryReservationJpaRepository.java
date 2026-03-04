package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservationEntity, Long> {
    Optional<InventoryReservationEntity> findByUuid(String uuid);
    Optional<InventoryReservationEntity> findByIdempotencyKey(String idempotencyKey);
    List<InventoryReservationEntity> findAllByInventoryItemUuid(String inventoryItemUuid);
    List<InventoryReservationEntity> findAllByOrderIdAndStatus(String orderId, InventoryReservationStatus status);
}
