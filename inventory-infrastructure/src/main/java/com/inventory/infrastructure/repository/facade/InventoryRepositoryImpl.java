package com.inventory.infrastructure.repository.facade;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.infrastructure.mapper.InventoryItemMapper;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryItemJpaRepository jpaRepository;
    private final InventoryItemMapper mapper;

    @Override
    public Optional<InventoryItem> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findBySku(String sku) {
        return jpaRepository.findAllBySku(sku).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<InventoryItem> findBySkuAndLocation(String sku, Id locationId) {
        return jpaRepository.findBySkuAndLocationId(sku, locationId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findByLocation(Id locationId) {
        return jpaRepository.findAllByLocationId(locationId.getValue()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findByProductVariantId(String productVariantId) {
        return jpaRepository.findAll().stream()
                .filter(e -> productVariantId.equals(e.getProductVariantId()))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findLowStock() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(InventoryItem::isLowStock)
                .toList();
    }

    @Override
    public List<InventoryItem> findNeedsReorder() {
        return jpaRepository.findItemsBelowReorderPoint().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findOutOfStock() {
        return jpaRepository.findAll().stream()
                .filter(e -> e.getStatus() == InventoryStatus.OUT_OF_STOCK)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public int getTotalAvailableQuantityBySku(String sku) {
        return jpaRepository.findAllBySku(sku).stream()
                .mapToInt(e -> e.getOnHand() - e.getReserved())
                .sum();
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return null;
    }

    @Override
    public void delete(Id id) {
        jpaRepository.findByUuid(id.getValue())
                .ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsBySkuAndLocation(String sku, Id locationId) {
        return jpaRepository.existsBySkuAndLocationId(sku, locationId.getValue());
    }
}
