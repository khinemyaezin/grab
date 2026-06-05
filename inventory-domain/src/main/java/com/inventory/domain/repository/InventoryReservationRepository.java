package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.InventoryReservation;

import java.util.Optional;

public interface InventoryReservationRepository {
    Optional<InventoryReservation> findById(Id id);
    Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey);
    void save(InventoryReservation reservation);
}
