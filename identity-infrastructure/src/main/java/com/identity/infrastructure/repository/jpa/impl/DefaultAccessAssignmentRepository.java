package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.valueobject.AccessScope;
import com.identity.infrastructure.entity.AccessAssignmentEntity;
import com.identity.infrastructure.mapper.jpa.AccessAssignmentJpaAssembler;
import com.identity.infrastructure.repository.jpa.AccessAssignmentJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultAccessAssignmentRepository implements AccessAssignmentRepository {
    private final AccessAssignmentJpaRepository assignments;
    private final AccessAssignmentJpaAssembler assembler;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<AccessAssignment> findById(Id id) {
        return executor.query("AccessAssignment", () -> assignments.findByUuid(id.getValue())
                .map(assembler::toDomain));
    }

    @Override
    public Optional<AccessAssignment> findCurrent(
            Id userId,
            String platformCode,
            String roleCode,
            AccessScope scope
    ) {
        return executor.query("AccessAssignment", () -> assignments.findCurrent(
                userId.getValue(), platformCode, roleCode, scope.type(), scope.scopeId()
        ).map(assembler::toDomain));
    }

    @Override
    public List<AccessAssignment> findEffectiveByUserAndPlatform(Id userId, String platformCode, Instant now) {
        return executor.query("AccessAssignment", () -> assignments
                .findEffectiveByUserAndPlatform(userId.getValue(), platformCode, now)
                .stream()
                .map(assembler::toDomain)
                .toList());
    }

    @Override
    public List<AccessAssignment> findByUser(Id userId) {
        return executor.query("AccessAssignment", () -> assignments
                .findByUser_UuidOrderByCreatedAt(userId.getValue())
                .stream()
                .map(assembler::toDomain)
                .toList());
    }

    @Override
    public boolean existsEffective(
            Id userId,
            String platformCode,
            String roleCode,
            AccessScope scope,
            Instant now
    ) {
        return executor.query("AccessAssignment", () -> assignments.existsEffective(
                userId.getValue(), platformCode, roleCode,
                scope.type(), scope.scopeId(), now
        ));
    }

    @Override
    public boolean existsCurrent(Id userId, String platformCode, String roleCode, AccessScope scope) {
        return executor.query("AccessAssignment", () -> assignments.existsCurrent(
                userId.getValue(), platformCode, roleCode, scope.type(), scope.scopeId()
        ));
    }

    @Override
    public AccessAssignment save(AccessAssignment assignment) {
        return executor.command("AccessAssignment", () -> {
            AccessAssignmentEntity existing = assignments.findByUuid(assignment.getId().getValue()).orElse(null);
            AccessAssignmentEntity saved = assignments.save(assembler.buildFullEntityGraph(assignment, existing));
            List<Event> events = assignment.pullEvents();
            domainEventProducer.produce(
                    assignment.getClass().getSimpleName(),
                    assignment.getId().getValue(),
                    events
            );
            return assembler.toDomain(saved);
        });
    }
}
