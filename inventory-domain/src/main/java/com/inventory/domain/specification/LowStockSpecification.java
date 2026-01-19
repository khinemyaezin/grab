package com.inventory.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.inventory.domain.aggregate.InventoryItem;

/**
 * Specification that checks if an inventory item has low stock.
 * An item is considered low stock when available quantity is at or below safety stock level.
 */
public class LowStockSpecification extends CompositeSpecification<InventoryItem> {

    @Override
    public boolean isSatisfiedBy(InventoryItem item) {
        if (item == null) return false;
        return item.isLowStock();
    }
}
