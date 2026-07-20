package com.inventory.domain.policy;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;

import java.util.List;
import java.util.Objects;

public final class VariantDeletedInventoryDiscontinuePolicy {

    private VariantDeletedInventoryDiscontinuePolicy() {
    }

    public static boolean shouldDiscontinue(InventoryItem item) {
        if (item == null) {
            return false;
        }
        return item.getStatus() != InventoryStatus.DISCONTINUED;
    }

    public static List<InventoryItem> selectForDiscontinue(List<InventoryItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .filter(VariantDeletedInventoryDiscontinuePolicy::shouldDiscontinue)
                .toList();
    }
}
