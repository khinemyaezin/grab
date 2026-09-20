package com.catalog.application.port.outbound;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductAuditPort {
    List<AuditEntry> findProductAuditTrail(String productId);

    record AuditEntry(String eventType, String status, LocalDateTime occurredAt, String payload) {
    }
}
