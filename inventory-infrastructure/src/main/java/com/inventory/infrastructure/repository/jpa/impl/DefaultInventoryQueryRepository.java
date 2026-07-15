package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.specification.jpa.InventorySearchSpecification;
import com.inventory.infrastructure.view.InventoryItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultInventoryQueryRepository implements InventoryQueryRepository {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";

    private final InventorySearchSpecification searchSpecification;
    private final PersistenceExecutor executor;

    @Override
    public Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> searchSpecification.search(criteria, pageable));
    }
}
