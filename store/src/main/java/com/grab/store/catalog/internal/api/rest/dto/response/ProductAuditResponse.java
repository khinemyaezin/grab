package com.grab.store.catalog.internal.api.rest.dto.response;

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
