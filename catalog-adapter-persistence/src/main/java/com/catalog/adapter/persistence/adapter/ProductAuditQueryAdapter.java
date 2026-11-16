package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.ProductAuditQueryPort;
import com.catalog.adapter.persistence.repository.CatalogOutboxEventJpaRepo;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RequiredArgsConstructor
public class ProductAuditQueryAdapter implements ProductAuditQueryPort {

    private final CatalogOutboxEventJpaRepo outboxEventJpaRepo;

    @Override
    public List<AuditEntry> findProductAuditTrail(String productId) {
        return outboxEventJpaRepo.findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("Product", productId)
                .stream()
                .map(event -> new AuditEntry(
                        event.getEventType(),
                        event.getStatus().name(),
                        toUtcLocalDateTime(event.getOccurredAt()),
                        event.getPayload()
                ))
                .toList();
    }

    private static LocalDateTime toUtcLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
