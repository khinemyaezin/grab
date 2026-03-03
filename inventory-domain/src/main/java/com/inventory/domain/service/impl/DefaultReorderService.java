package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.ReorderService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DefaultReorderService implements ReorderService {

    private final InventoryRepository inventoryRepository;

    public DefaultReorderService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestions() {
        return inventoryRepository.findAll().stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();
    }

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestionsForLocation(Id locationId) {
        return inventoryRepository.findByLocation(locationId).stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();
    }

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestionsForSku(String sku) {
        return inventoryRepository.findBySku(sku).stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();
    }

    @Override
    public List<InventoryItem> getCriticalReorderItems() {
        return Stream.concat(
                        inventoryRepository.findOutOfStock().stream(),
                        inventoryRepository.findLowStock().stream()
                )
                .filter(InventoryItem::isActive)
                .distinct()
                .toList();
    }

    @Override
    public ReorderPriority calculatePriority(InventoryItem item) {
        int available = item.getAvailableQuantity();
        int safetyStock = item.getReorderConfig().safetyStock();
        int reorderPoint = item.getReorderConfig().reorderPoint();

        if (available <= 0) {
            return ReorderPriority.CRITICAL;
        }
        if (available <= safetyStock) {
            return ReorderPriority.CRITICAL;
        }
        if (available <= reorderPoint) {
            return ReorderPriority.HIGH;
        }
        if (available <= reorderPoint * 1.2) {
            return ReorderPriority.MEDIUM;
        }
        return ReorderPriority.LOW;
    }

    private boolean shouldSuggestReorder(InventoryItem item) {
        ReorderPriority priority = calculatePriority(item);
        return priority != ReorderPriority.LOW;
    }

    private ReorderSuggestion createSuggestion(InventoryItem item) {
        return new ReorderSuggestion(
                item.getId(),
                item.getSku(),
                item.getProductVariantId(),
                item.getLocationId(),
                item.getAvailableQuantity(),
                item.getReorderConfig().reorderPoint(),
                item.getSuggestedReorderQuantity(),
                calculatePriority(item)
        );
    }
}
