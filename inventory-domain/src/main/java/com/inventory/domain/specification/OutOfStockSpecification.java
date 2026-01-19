package com.inventory.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.inventory.domain.aggregate.InventoryItem;

/**
 * Specification that checks if an inventory item is out of stock.
 */
public class OutOfStockSpecification extends CompositeSpecification<InventoryItem> {

    @Override
    public boolean isSatisfiedBy(InventoryItem item) {
        if (item == null) return false;
        return item.isOutOfStock();
    }
}
