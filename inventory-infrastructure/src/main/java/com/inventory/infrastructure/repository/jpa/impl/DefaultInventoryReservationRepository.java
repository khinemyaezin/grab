package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultInventoryReservationRepository implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;
    private final IdGenerator idGenerator;

    @Override
    public Optional<InventoryReservation> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public List<InventoryReservation> findByInventoryItemId(Id inventoryItemId) {
        return jpaRepository.findAllByInventoryItemUuid(inventoryItemId.getValue()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<InventoryReservation> findActiveByOrderId(String orderId) {
        return jpaRepository.findAllByOrderIdAndStatus(orderId, com.inventory.domain.enums.InventoryReservationStatus.ACTIVE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void save(InventoryReservation reservation) {
        InventoryReservationEntity entity = jpaRepository.findByUuid(reservation.getId().getValue())
                .orElse(new InventoryReservationEntity());

        entity.setUuid(reservation.getId().getValue());
        entity.setInventoryItemUuid(reservation.getInventoryItemId().getValue());
        entity.setOrderId(reservation.getOrderId());
        entity.setOrderLineId(reservation.getOrderLineId());
        entity.setQuantity(reservation.getQuantity());
        entity.setStatus(reservation.getStatus());
        entity.setExpiresAt(reservation.getExpiresAt());
        entity.setIdempotencyKey(reservation.getIdempotencyKey());
        entity.setCreatedAt(reservation.getCreatedAt());
        entity.setUpdatedAt(reservation.getUpdatedAt());
        jpaRepository.save(entity);
    }

    private InventoryReservation toDomain(InventoryReservationEntity entity) {
        return new InventoryReservation(
                idGenerator.generateId(entity.getUuid()),
                idGenerator.generateId(entity.getInventoryItemUuid()),
                entity.getOrderId(),
                entity.getOrderLineId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
