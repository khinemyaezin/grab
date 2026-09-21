package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityOutboxEventJpaRepo extends JpaRepository<IdentityOutboxEvent, Long> {
    List<IdentityOutboxEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc(String aggregateType, String aggregateId);
}
