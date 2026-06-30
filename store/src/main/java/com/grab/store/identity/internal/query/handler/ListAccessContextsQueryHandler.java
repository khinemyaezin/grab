package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.AccessContextResult;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.repository.AccessAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListAccessContextsQueryHandler
        implements QueryHandler<ListAccessContextsQuery, List<AccessContextResult>> {
    private final AccessAssignmentRepository assignments;

    @Override
    @IdentityReadTransactional
    public List<AccessContextResult> handle(ListAccessContextsQuery query) {
        Map<ContextKey, List<AccessAssignment>> contexts = assignments
                .findEffectiveByUserAndPlatform(query.userId(), query.platformCode(), Instant.now())
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> new ContextKey(
                                assignment.getScope().key().value(),
                                assignment.getScope().scopeId()
                        ),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return contexts.values().stream()
                .map(this::toResult)
                .toList();
    }

    private AccessContextResult toResult(List<AccessAssignment> contextAssignments) {
        AccessAssignment anchor = contextAssignments.getFirst();
        Set<String> roleCodes = contextAssignments.stream()
                .map(AccessAssignment::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String expiresAt = contextAssignments.stream().anyMatch(assignment -> assignment.getExpiresAt() == null)
                ? null
                : contextAssignments.stream()
                .map(AccessAssignment::getExpiresAt)
                .max(Instant::compareTo)
                .map(Instant::toString)
                .orElse(null);

        return new AccessContextResult(
                anchor.getId().getValue(),
                anchor.getPlatformCode(),
                Set.copyOf(roleCodes),
                anchor.getScope().key().value(),
                anchor.getScope().scopeId(),
                expiresAt
        );
    }

    @Override
    public Class<ListAccessContextsQuery> getQueryType() {
        return ListAccessContextsQuery.class;
    }

    private record ContextKey(String scopeKey, String scopeId) {
    }
}
