package com.identity.infrastructure.repository.jpa;

import com.grab.framework.outbox.OutboxStatus;
import com.identity.infrastructure.outbox.IdentityOutboxEvent;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IdentityOutboxEventJpaRepoTest extends RepositoryTestConfig {

    @Autowired
    private IdentityOutboxEventJpaRepo outboxEventJpaRepo;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepo.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        IdentityOutboxEvent event1 = new IdentityOutboxEvent();
        event1.setAggregateType("User");
        event1.setAggregateId("user-1");
        event1.setEventType("UserCreated");
        event1.setEventVersion(1);
        event1.setHeaders("{}");
        event1.setPayload("{\"uuid\":\"user-1\"}");
        event1.setStatus(OutboxStatus.NEW);
        event1.setOccurredAt(now.minusMinutes(10));
        event1.setAvailableAt(now.minusMinutes(10));

        IdentityOutboxEvent event2 = new IdentityOutboxEvent();
        event2.setAggregateType("User");
        event2.setAggregateId("user-1");
        event2.setEventType("UserUpdated");
        event2.setEventVersion(1);
        event2.setHeaders("{}");
        event2.setPayload("{\"uuid\":\"user-1\",\"status\":\"ACTIVE\"}");
        event2.setStatus(OutboxStatus.PUBLISHED);
        event2.setOccurredAt(now.minusMinutes(5));
        event2.setAvailableAt(now.minusMinutes(5));

        IdentityOutboxEvent event3 = new IdentityOutboxEvent();
        event3.setAggregateType("Role");
        event3.setAggregateId("role-1");
        event3.setEventType("RoleCreated");
        event3.setEventVersion(1);
        event3.setHeaders("{}");
        event3.setPayload("{\"uuid\":\"role-1\"}");
        event3.setStatus(OutboxStatus.NEW);
        event3.setOccurredAt(now.minusMinutes(3));
        event3.setAvailableAt(now.minusMinutes(3));

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
}
