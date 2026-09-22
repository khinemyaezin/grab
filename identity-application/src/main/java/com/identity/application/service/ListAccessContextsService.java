package com.identity.application.service;

import com.identity.application.port.inbound.ListAccessContextsUseCase;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.read.AccessAssignmentView;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListAccessContextsService implements ListAccessContextsUseCase {
    private final AccessAssignmentQueryPort assignments;

    public List<AccessContextResult> execute(ListAccessContextsQuery query) {
        Instant now = Instant.now();
        Map<ContextKey, List<AccessAssignmentView>> contexts = assignments
                .findEffectiveByUserAndPlatform(query.userId().getValue(), query.platformCode(), now)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> new ContextKey(assignment.scopeKey(), assignment.scopeId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return contexts.values().stream()
                .map(this::toResult)
                .toList();
    }

    private AccessContextResult toResult(List<AccessAssignmentView> contextAssignments) {
        AccessAssignmentView anchor = contextAssignments.getFirst();
        Set<String> roleCodes = contextAssignments.stream()
                .map(AccessAssignmentView::roleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String expiresAt = contextAssignments.stream().anyMatch(a -> a.expiresAt() == null)
                ? null
                : contextAssignments.stream()
                .map(AccessAssignmentView::expiresAt)
                .max(Instant::compareTo)
                .map(Instant::toString)
                .orElse(null);

        return new AccessContextResult(
                anchor.id(),
                anchor.platformCode(),
                Set.copyOf(roleCodes),
                anchor.scopeKey(),
                anchor.scopeId(),
                expiresAt
        );
    }

    private record ContextKey(String scopeKey, String scopeId) {
    }
}
