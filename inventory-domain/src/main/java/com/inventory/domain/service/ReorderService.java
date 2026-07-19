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
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    List<ReorderSuggestion> calculateReorderSuggestions(Id merchantId);
    List<ReorderSuggestion> calculateReorderSuggestionsForLocation(Id locationId);
    List<ReorderSuggestion> calculateReorderSuggestionsForSku(String sku);
    List<InventoryItem> getCriticalReorderItems(Id merchantId);
    ReorderPriority calculatePriority(InventoryItem item);
}
