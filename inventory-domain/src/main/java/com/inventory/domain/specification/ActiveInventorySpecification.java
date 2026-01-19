package com.inventory.domain.specification;

import com.grab.framework.specification.CompositeSpecification;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;

public class ActiveInventorySpecification extends CompositeSpecification<InventoryItem> {

    @Override
    public boolean isSatisfiedBy(InventoryItem item) {
        if (item == null) return false;
        return item.getStatus() == InventoryStatus.ACTIVE;
    }
}
