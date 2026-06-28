package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.ListAccessAssignmentsQuery;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListAccessAssignmentsQueryHandler
        implements QueryHandler<ListAccessAssignmentsQuery, List<AccessAssignmentResult>> {
    private final AccessAssignmentRepository assignments;

    @Override
    @IdentityReadTransactional
    public List<AccessAssignmentResult> handle(ListAccessAssignmentsQuery query) {
        AccessScope actorScope = AccessScope.from(query.actorScopeKey(), query.actorScopeId());
        Instant now = Instant.now();
        return assignments.findByUser(query.userId()).stream()
                .filter(assignment -> actorScope.encompasses(assignment.getScope()))
                .map(assignment -> AccessAssignmentResult.from(assignment, now))
                .toList();
    }

    @Override
    public Class<ListAccessAssignmentsQuery> getQueryType() {
        return ListAccessAssignmentsQuery.class;
    }
}
