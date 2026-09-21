package com.inventory.application.port.outbound;

import com.grab.framework.id.Id;
import com.inventory.application.model.read.InventoryReservationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryReservationQueryPort {
    Page<InventoryReservationView> queryByInventoryItemId(String inventoryItemUuid, Pageable pageable);
    Page<InventoryReservationView> queryActiveByOrderId(String orderId, Pageable pageable);
}
