package com.inventory.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.inventory.domain.aggregate.InventoryItem;

public class SufficientStockSpecification extends CompositeSpecification<InventoryItem> {

    private final int requiredQuantity;

    public SufficientStockSpecification(int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("Required quantity must be positive");
        }
        this.requiredQuantity = requiredQuantity;
    }

    @Override
    public boolean isSatisfiedBy(InventoryItem item) {
        if (item == null) return false;
        return item.canFulfill(requiredQuantity);
    }
}
