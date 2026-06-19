package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.outbox.IdentityOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityOutboxEventJpaRepo extends JpaRepository<IdentityOutboxEvent, Long> {
    List<IdentityOutboxEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc(String aggregateType, String aggregateId);
}
