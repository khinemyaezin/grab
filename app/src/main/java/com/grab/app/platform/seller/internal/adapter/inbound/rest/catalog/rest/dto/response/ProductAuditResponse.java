package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductAuditResponse(
        String productId,
        List<Entry> entries
) {
    public record Entry(
            String eventType,
            String status,
            LocalDateTime occurredAt,
            String payload
    ) {
    }
}
