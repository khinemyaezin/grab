package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.entity.InventoryReservation;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryReservationEntityMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryReservationJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.InventoryReservationMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryReservationJpaAssemblerImpl implements InventoryReservationJpaAssembler {
    private final InventoryReservationEntityMapper inventoryReservationEntityMapper;
    private final InventoryReservationMapper inventoryReservationMapper;

    @Override
    public InventoryReservationEntity buildFullEntityGraph(InventoryReservation reservation, InventoryReservationEntity entity) {
        if (entity == null) {
            entity = new InventoryReservationEntity();
        }
        inventoryReservationEntityMapper.toEntity(reservation, entity);
        return entity;
    }

    @Override
    public InventoryReservation toFullDomainGraph(InventoryReservationEntity reservationEntity) {
        return inventoryReservationMapper.toDomain(reservationEntity);
    }
}
