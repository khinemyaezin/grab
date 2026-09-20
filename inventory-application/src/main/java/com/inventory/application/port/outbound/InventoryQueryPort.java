package com.inventory.application.port.outbound;

import com.inventory.application.model.read.InventorySearchCriteria;
import com.inventory.application.model.read.InventoryExistenceView;
import com.inventory.application.model.read.InventoryItemView;
import com.inventory.application.model.read.InventorySummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryQueryPort {
    Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable);

    List<InventoryExistenceView> findExistenceByMerchantLocationAndSkus(
            String merchantId,
            String locationId,
            Collection<String> skus
    );

    InventorySummaryView summarize(String merchantId, String locationId);

    Optional<InventoryItemView> findById(String inventoryItemId);

    boolean existsActiveRoute(String merchantId, String salesChannelId);

    int sumAvailableForAllocation(String sku, String salesChannelId);

    List<String> findSkusByLocation(String locationId);

    List<InventoryItemView> findReorderCandidates(String merchantId, String locationId);
}
