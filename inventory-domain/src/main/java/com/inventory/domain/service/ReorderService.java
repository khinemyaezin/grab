package com.inventory.domain.service;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;

import java.util.List;

public interface ReorderService {
    record ReorderSuggestion(
            Id inventoryItemId,
            String sku,
            Id productVariantId,
            Id locationId,
            int currentAvailable,
            int reorderPoint,
            int suggestedQuantity,
            ReorderPriority priority
    ) {}

    enum ReorderPriority {
        CRITICAL,   // Out of stock or below safety stock
        HIGH,       // At or below reorder point
        MEDIUM,     // Approaching reorder point (within 20%)
        LOW         // Normal levels but suggested for optimization
    }

    List<ReorderSuggestion> calculateReorderSuggestions();
    List<ReorderSuggestion> calculateReorderSuggestionsForLocation(Id locationId);
    List<ReorderSuggestion> calculateReorderSuggestionsForSku(String sku);
    List<InventoryItem> getCriticalReorderItems();
    ReorderPriority calculatePriority(InventoryItem item);
}
