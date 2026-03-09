package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.entity.InventoryReservation;
import com.inventory.infrastructure.entity.InventoryReservationEntity;

public interface InventoryReservationJpaAssembler {
    /**
     * If `entity` is null a new InventoryReservationEntity instance will be created; otherwise the provided `entity` is updated.
     *
     * @param reservation the full InventoryReservation entity to persist
     * @param entity the existing InventoryReservationEntity to update, or null to create a new instance
     * @return the resulting InventoryReservationEntity populated from the domain entity
     */
    InventoryReservationEntity buildFullEntityGraph(InventoryReservation reservation, InventoryReservationEntity entity);

    /**
     * Reconstruct the InventoryReservation domain entity from the provided JPA entity.
     *
     * @param reservationEntity the persisted InventoryReservationEntity to convert (may not be null)
     * @return a fully populated InventoryReservation domain entity
     */
    InventoryReservation toFullDomainGraph(InventoryReservationEntity reservationEntity);
}
