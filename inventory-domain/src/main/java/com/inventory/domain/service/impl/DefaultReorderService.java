package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.ReorderService;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public class DefaultReorderService implements ReorderService {
    private static final Logger log = Loggers.getLogger(DefaultReorderService.class);

    private final InventoryRepository inventoryRepository;

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestions(Id sellerId) {
        log.info("Calculating reorder suggestions for all inventory items");

        List<ReorderSuggestion> suggestions = inventoryRepository.findAll(sellerId).stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();

        log.info("Calculated {} reorder suggestions across all locations", suggestions.size());
        return suggestions;
    }

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestionsForLocation(Id locationId) {
        log.info("Calculating reorder suggestions for locationId={}", locationId.getValue());

        List<ReorderSuggestion> suggestions = inventoryRepository.findByLocation(locationId).stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();

        log.info("Calculated {} reorder suggestions for locationId={}", suggestions.size(), locationId.getValue());
        return suggestions;
    }

    @Override
    public List<ReorderSuggestion> calculateReorderSuggestionsForSku(String sku) {
        log.info("Calculating reorder suggestions for sku={}", sku);

        List<ReorderSuggestion> suggestions = inventoryRepository.findBySku(sku).stream()
                .filter(InventoryItem::isActive)
                .filter(this::shouldSuggestReorder)
                .map(this::createSuggestion)
                .sorted(Comparator.comparing(ReorderSuggestion::priority))
                .toList();

        log.info("Calculated {} reorder suggestions for sku={}", suggestions.size(), sku);
        return suggestions;
    }

    @Override
    public List<InventoryItem> getCriticalReorderItems(Id sellerId) {
        List<InventoryItem> criticalItems = Stream.concat(
                        inventoryRepository.findOutOfStock(sellerId).stream(),
                        inventoryRepository.findLowStock(sellerId).stream()
                )
                .filter(InventoryItem::isActive)
                .distinct()
                .toList();

        log.info("Found {} critical reorder items", criticalItems.size());
        return criticalItems;
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
        log.debug(
                "Calculated reorder priority={} for inventoryItemId={}, sku={}, available={}",
                priority,
                item.getId().getValue(),
                item.getSku(),
                item.getAvailableQuantity()
        );
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
