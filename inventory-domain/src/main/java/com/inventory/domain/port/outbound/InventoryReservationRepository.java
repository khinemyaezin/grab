package com.inventory.domain.port.outbound;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.InventoryReservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository {
    Optional<InventoryReservation> findById(Id id);
    Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey);
    List<InventoryReservation> findActiveByOrderId(String orderId);
    List<InventoryReservation> findExpiredActive(LocalDateTime asOf, int limit);
    void save(InventoryReservation reservation);
}
