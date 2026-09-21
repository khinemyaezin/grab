package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogOutboxEventJpaRepo extends JpaRepository<CatalogOutboxEvent, Long> {
    List<CatalogOutboxEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc(String aggregateType, String aggregateId);
}
