package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryJpaAssembler;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.support.InventoryPersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultInventoryRepository implements InventoryRepository {

    private final InventoryItemJpaRepository jpaRepository;
    private final InventoryJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<InventoryItem> findById(Id id) {
        return executor.query("InventoryItem", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<InventoryItem> findBySku(String sku) {
        return executor.query("InventoryItem", () -> jpaRepository.findAllBySku(sku).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public Optional<InventoryItem> findBySkuAndLocation(String sku, Id locationId) {
        return executor.query("InventoryItem", () -> jpaRepository.findBySkuAndLocationId(sku, locationId.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<InventoryItem> findByLocation(Id locationId) {
        return executor.query("InventoryItem", () -> jpaRepository.findAllByLocationId(locationId.getValue()).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<InventoryItem> findByProductVariantId(String productVariantId) {
        return executor.query("InventoryItem", () -> jpaRepository.findAll().stream()
                .filter(e -> productVariantId.equals(e.getProductVariantId()))
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<InventoryItem> findLowStock() {
        return executor.query("InventoryItem", () -> jpaRepository.findAll().stream()
                .map(mapper::toFullDomainGraph)
                .filter(InventoryItem::isLowStock)
                .toList());
    }

    @Override
    public List<InventoryItem> findNeedsReorder() {
        return executor.query("InventoryItem", () -> jpaRepository.findItemsBelowReorderPoint().stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public List<InventoryItem> findOutOfStock() {
        return executor.query("InventoryItem", () -> jpaRepository.findAll().stream()
                .filter(e -> e.getStatus() == InventoryStatus.OUT_OF_STOCK)
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public int getTotalAvailableQuantityBySku(String sku) {
        return executor.query("InventoryItem", () -> jpaRepository.findAllBySku(sku).stream()
                .mapToInt(e -> Math.max(0, e.getOnHand() - e.getReserved() - e.getDamaged()))
                .sum());
    }

    @Override
    public List<InventoryItem> findAll() {
        return executor.query("InventoryItem", () -> jpaRepository.findAll().stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public void save(InventoryItem item) {
        executor.command("InventoryItem", () -> {
            Optional<InventoryItemEntity> existingEntity = jpaRepository.findByUuid(item.getId().getValue());
            InventoryItemEntity entity;

            if (existingEntity.isPresent()) {
                entity = mapper.buildFullEntityGraph(item, existingEntity.get());
            } else {
                entity = mapper.buildFullEntityGraph(item, null);
            }

            jpaRepository.save(entity);

            List<Event> events = item.pullEvents();
            domainEventProducer.produce(item.getClass().getSimpleName(), item.getId().getValue(), events);
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("InventoryItem", () -> jpaRepository.findByUuid(id.getValue())
                .ifPresent(jpaRepository::delete));
    }

    @Override
    public boolean existsBySkuAndLocation(String sku, Id locationId) {
        return executor.query("InventoryItem", () -> jpaRepository.existsBySkuAndLocationId(sku, locationId.getValue()));
    }
}
