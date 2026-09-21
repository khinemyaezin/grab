package com.identity.application.service;

import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.port.inbound.ListAccessAssignmentsUseCase;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ListAccessAssignmentsService implements ListAccessAssignmentsUseCase {
    private final AccessAssignmentQueryPort assignments;

    public List<AccessAssignmentResult> execute(ListAccessAssignmentsQuery query) {
        AccessScope actorScope = AccessScope.from(query.actorScopeKey(), query.actorScopeId());
        Instant now = Instant.now();
        return assignments.findByUser(query.userId().getValue()).stream()
                .filter(assignment -> actorScope.encompasses(
                        AccessScope.from(assignment.scopeKey(), assignment.scopeId())))
                .map(assignment -> toResult(assignment, now))
                .toList();
    }

    private AccessAssignmentResult toResult(
            com.identity.application.model.read.AccessAssignmentView assignment,
            Instant at
    ) {
        return new AccessAssignmentResult(
                assignment.id(),
                assignment.userId(),
                assignment.platformCode(),
                assignment.roleCode(),
                assignment.scopeKey(),
                assignment.scopeId(),
                assignment.effectiveStatus().name(),
                assignment.assignedBy(),
                assignment.createdAt().toString(),
                assignment.updatedAt().toString(),
                assignment.expiresAt() == null ? null : assignment.expiresAt().toString()
        );
    }
}
