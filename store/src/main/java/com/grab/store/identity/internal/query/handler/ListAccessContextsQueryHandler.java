package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.AccessContextResult;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import com.identity.domain.repository.AccessAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListAccessContextsQueryHandler
        implements QueryHandler<ListAccessContextsQuery, List<AccessContextResult>> {
    private final AccessAssignmentRepository assignments;

    @Override
    @IdentityReadTransactional
    public List<AccessContextResult> handle(ListAccessContextsQuery query) {
        return assignments.findEffectiveByUserAndPlatform(query.userId(), query.platformCode(), Instant.now())
                .stream()
                .map(assignment -> new AccessContextResult(
                        assignment.getId().getValue(),
                        assignment.getPlatformCode(),
                        assignment.getRoleCode(),
                        assignment.getScope().type().name(),
                        assignment.getScope().scopeId(),
                        assignment.getExpiresAt() == null ? null : assignment.getExpiresAt().toString()
                ))
                .toList();
    }

    @Override
    public Class<ListAccessContextsQuery> getQueryType() {
        return ListAccessContextsQuery.class;
    }
}
