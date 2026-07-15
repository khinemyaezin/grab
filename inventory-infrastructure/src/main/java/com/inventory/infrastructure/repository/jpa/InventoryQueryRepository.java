package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.view.InventoryItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryQueryRepository {
    Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable);
}
