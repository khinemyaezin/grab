package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryReservationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.InventoryReservationJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryReservationQueryRepository;
import com.inventory.infrastructure.view.InventoryReservationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class DefaultInventoryReservationRepository implements InventoryReservationRepository, InventoryReservationQueryRepository {

    private static final Logger log = Loggers.getLogger(DefaultInventoryReservationRepository.class);

    private final InventoryReservationJpaRepository jpaRepository;
    private final InventoryReservationJpaAssembler mapper;
    private final PersistenceExecutor executor;

    @Override
    public Optional<InventoryReservation> findById(Id id) {
        log.debug("Loading inventory reservation by id={}", id.getValue());
        return executor.query("InventoryReservation", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public void save(InventoryReservation reservation) {
        executor.command("InventoryReservation", () -> {
            log.info("Persisting inventory reservation id={}", reservation.getId().getValue());
            Optional<InventoryReservationEntity> existingEntity = jpaRepository.findByUuid(reservation.getId().getValue());
            InventoryReservationEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(reservation, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(reservation, null);
            }

            jpaRepository.save(entity);
            log.debug("Persisted inventory reservation id={}", reservation.getId().getValue());
            return null;
        });
    }

    @Override
    public Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Loading inventory reservation by idempotencyKey={}", idempotencyKey);
        return executor.query("InventoryReservation", () -> jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toFullDomainGraph));
    }

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
