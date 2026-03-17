package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.outbox.CatalogOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogOutboxEventJpaRepo extends JpaRepository<CatalogOutboxEvent, Long> {
    List<CatalogOutboxEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc(String aggregateType, String aggregateId);
}
