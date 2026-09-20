package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.ProductAuditPort;
import com.catalog.adapter.persistence.repository.CatalogOutboxEventJpaRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProductAuditAdapter implements ProductAuditPort {

    private final CatalogOutboxEventJpaRepo outboxEventJpaRepo;

    @Override
    public List<AuditEntry> findProductAuditTrail(String productId) {
        return outboxEventJpaRepo.findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("Product", productId)
                .stream()
                .map(event -> new AuditEntry(
                        event.getEventType(),
                        event.getStatus().name(),
                        event.getOccurredAt(),
                        event.getPayload()
                ))
                .toList();
    }
}
