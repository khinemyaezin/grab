package com.inventory.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.inventory.infrastructure.view.InventoryReservationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryReservationQueryRepository {
    Page<InventoryReservationView> queryByInventoryItemId(String inventoryItemUuid, Pageable pageable);
    Page<InventoryReservationView> queryActiveByOrderId(String orderId, Pageable pageable);
}
