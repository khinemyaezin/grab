package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.view.InventoryExistenceView;
import com.inventory.infrastructure.view.InventoryItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface InventoryQueryRepository {
    Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable);

    List<InventoryExistenceView> findExistenceByMerchantLocationAndSkus(
            String merchantId,
            String locationId,
            Collection<String> skus
    );
}
