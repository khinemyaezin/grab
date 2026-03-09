package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
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
    private final PersistenceExecutor executor;

    @Override
    public Optional<InventoryReservation> findById(Id id) {
        return executor.query("InventoryReservation", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey) {
        return executor.query("InventoryReservation", () -> jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<InventoryReservation> findByInventoryItemId(Id inventoryItemId) {
        return executor.query("InventoryReservation", () -> jpaRepository.findAllByInventoryItemUuid(inventoryItemId.getValue()).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<InventoryReservation> findActiveByOrderId(String orderId) {
        return executor.query("InventoryReservation", () -> jpaRepository.findAllByOrderIdAndStatus(orderId, com.inventory.domain.enums.InventoryReservationStatus.ACTIVE).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public void save(InventoryReservation reservation) {
        executor.command("InventoryReservation", () -> {
            Optional<InventoryReservationEntity> existingEntity = jpaRepository.findByUuid(reservation.getId().getValue());
            InventoryReservationEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(reservation, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(reservation, null);
            }

            jpaRepository.save(entity);
            return null;
        });
    }
}
