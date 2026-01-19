package com.inventory.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.inventory.domain.aggregate.InventoryItem;

/**
 * Specification that checks if an inventory item needs to be reordered.
 * An item needs reorder when available quantity is at or below the reorder point.
 */
public class NeedsReorderSpecification extends CompositeSpecification<InventoryItem> {

    @Override
    public boolean isSatisfiedBy(InventoryItem item) {
        if (item == null) return false;
        return item.needsReorder() && item.isActive();
    }
}
