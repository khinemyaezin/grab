package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryReservationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultInventoryReservationRepository implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;
    private final InventoryReservationJpaAssembler mapper;

    @Override
    public Optional<InventoryReservation> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph);
    }

    @Override
    public Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toFullDomainGraph);
    }

    @Override
    public List<InventoryReservation> findByInventoryItemId(Id inventoryItemId) {
        return jpaRepository.findAllByInventoryItemUuid(inventoryItemId.getValue()).stream()
                .map(mapper::toFullDomainGraph)
                .toList();
    }

    @Override
    public List<InventoryReservation> findActiveByOrderId(String orderId) {
        return jpaRepository.findAllByOrderIdAndStatus(orderId, com.inventory.domain.enums.InventoryReservationStatus.ACTIVE).stream()
                .map(mapper::toFullDomainGraph)
                .toList();
    }

    @Override
    public void save(InventoryReservation reservation) {
        Optional<InventoryReservationEntity> existingEntity = jpaRepository.findByUuid(reservation.getId().getValue());
        InventoryReservationEntity entity;

        if (existingEntity.isPresent()) {
            entity = mapper.buildFullEntityGraph(reservation, existingEntity.get());
        } else {
            entity = mapper.buildFullEntityGraph(reservation, null);
        }

        jpaRepository.save(entity);
    }
}
