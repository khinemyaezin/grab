package com.catalog.application.model.read;

import java.time.LocalDateTime;
import java.util.List;

public record GetProductAuditResult(
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
