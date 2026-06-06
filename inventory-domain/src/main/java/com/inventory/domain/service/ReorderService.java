package com.inventory.domain.service;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;

import java.util.List;

/** Service for calculating reorder suggestions based on inventory levels and reorder configurations.
 * <p>This service analyzes the current inventory levels against predefined reorder points and generates suggestions for replenishment.
 * It helps ensure that stock levels are maintained to meet demand while minimizing excess inventory.</p>
 * Feature documentation: <br>
 * See docs/features/reorder-service.md
 **/
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

    List<ReorderSuggestion> calculateReorderSuggestions(Id sellerId);
    List<ReorderSuggestion> calculateReorderSuggestionsForLocation(Id locationId);
    List<ReorderSuggestion> calculateReorderSuggestionsForSku(String sku);
    List<InventoryItem> getCriticalReorderItems(Id sellerId);
    ReorderPriority calculatePriority(InventoryItem item);
}
