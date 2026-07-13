package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryJpaAssembler;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultInventoryRepository implements InventoryRepository {

    private static final Logger log = Loggers.getLogger(DefaultInventoryRepository.class);

    private final InventoryItemJpaRepository jpaRepository;
    private final InventoryJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<InventoryItem> findById(Id id) {
        log.debug("Loading inventory item by id={}", id.getValue());
        return executor.query("InventoryItem", () -> jpaRepository.findByUuid(id.getValue())
                .map(mapper::toFullDomainGraph));
    }

    @Override
    public List<InventoryItem> findBySku(String sku) {
        log.debug("Loading inventory items by sku={}", sku);
        return executor.query("InventoryItem", () -> jpaRepository.findAllBySku(sku).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public Optional<InventoryItem> findBySkuAndLocation(String sku, Id locationId) {
        log.debug("Loading inventory item by sku={} and locationId={}", sku, locationId.getValue());
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
    public List<InventoryItem> findLowStock(Id sellerId) {
        return executor.query("InventoryItem", () ->
                jpaRepository.findLowStockItemsAndSellerId(sellerId.getValue()).stream()
                        .map(mapper::toFullDomainGraph)
                        .toList());
    }

    @Override
    public List<InventoryItem> findOutOfStock(Id sellerId) {
        return executor.query("InventoryItem", () ->
                jpaRepository.findAllByStatusAndSellerId(InventoryStatus.OUT_OF_STOCK, sellerId.getValue()).stream()
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
    public List<InventoryItem> findAll(Id sellerId) {
        return executor.query("InventoryItem", () -> jpaRepository.findAllBySellerId(sellerId.getValue()).stream()
                .map(mapper::toFullDomainGraph)
                .toList());
    }

    @Override
    public void save(InventoryItem item) {
        executor.command("InventoryItem", () -> {
            log.info("Persisting inventory item id={}, sku={}", item.getId().getValue(), item.getSku());
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
            log.info(
                    "Persisted inventory item id={}, sku={}, publishedEvents={}",
                    item.getId().getValue(),
                    item.getSku(),
                    events.size()
            );
        });
    }

    @Override
    public void delete(Id id) {
        executor.command("InventoryItem", () -> {
            log.info("Deleting inventory item id={}", id.getValue());
            jpaRepository.findByUuid(id.getValue())
                    .ifPresent(jpaRepository::delete);
        });
    }

    @Override
    public boolean existsBySkuAndLocation(String sku, Id locationId) {
        return executor.query("InventoryItem", () -> jpaRepository.existsBySkuAndLocationId(sku, locationId.getValue()));
    }
}
