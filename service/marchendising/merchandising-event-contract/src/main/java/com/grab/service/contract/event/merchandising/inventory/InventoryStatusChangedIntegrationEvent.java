package com.grab.service.contract.event.merchandising.inventory;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record InventoryStatusChangedIntegrationEvent(
        String sku,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
