package com.identity.adapter.persistence.repository.jpa;

import com.grab.framework.outbox.SerializedEvent;
import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import com.identity.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class IdentityOutboxEventJpaRepoTest extends RepositoryTestConfig {

    @Autowired
    private IdentityOutboxEventJpaRepo outboxEventJpaRepo;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepo.deleteAll();

        Instant now = Instant.now();

        IdentityOutboxEvent event1 = row("User", "user-1", "UserCreated", "{\"uuid\":\"user-1\"}", now.minus(10, ChronoUnit.MINUTES));
        IdentityOutboxEvent event2 = row("User", "user-1", "UserUpdated", "{\"uuid\":\"user-1\",\"status\":\"ACTIVE\"}", now.minus(5, ChronoUnit.MINUTES));
        event2.markProcessing(now, "claim");
        event2.markPublished(now);
        IdentityOutboxEvent event3 = row("Role", "role-1", "RoleCreated", "{\"uuid\":\"role-1\"}", now.minus(3, ChronoUnit.MINUTES));

        outboxEventJpaRepo.saveAll(List.of(event1, event2, event3));
    }

    @Test
    void findByAggregateTypeAndAggregateId_returnsEvents_orderedByOccurredAtDesc() {
        List<IdentityOutboxEvent> result = outboxEventJpaRepo
                .findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("User", "user-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEventType()).isEqualTo("UserUpdated");
        assertThat(result.get(1).getEventType()).isEqualTo("UserCreated");
    }

    @Test
    void findByAggregateTypeAndAggregateId_returnsEmpty_whenAggregateTypeMismatch() {
        List<IdentityOutboxEvent> result = outboxEventJpaRepo
                .findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("Authority", "user-1");

        assertThat(result).isEmpty();
    }

    @Test
    void findByAggregateTypeAndAggregateId_returnsEmpty_whenAggregateIdMismatch() {
        List<IdentityOutboxEvent> result = outboxEventJpaRepo
                .findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("User", "user-999");

        assertThat(result).isEmpty();
    }

    @Test
    void findByAggregateTypeAndAggregateId_returnsSingleEvent_forRoleAggregate() {
        List<IdentityOutboxEvent> result = outboxEventJpaRepo
                .findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("Role", "role-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo("RoleCreated");
        assertThat(result.get(0).getAggregateId()).isEqualTo("role-1");
    }

    @Test
    void findAll_returnsAllOutboxEvents() {
        List<IdentityOutboxEvent> result = outboxEventJpaRepo.findAll();

        assertThat(result).hasSize(3);
    }

    private static IdentityOutboxEvent row(String aggregateType, String aggregateId, String eventType, String payload, Instant occurredAt) {
        return IdentityOutboxEvent.pending(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                new SerializedEvent(eventType, payload, 1, "{}"),
                occurredAt
        );
    }
}
