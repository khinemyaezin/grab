package com.identity.application.service;

import com.identity.application.port.inbound.ListAccessContextsUseCase;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.MerchantViewQueryPort;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.model.read.MerchantView;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListAccessContextsService implements ListAccessContextsUseCase {
    private final AccessAssignmentQueryPort assignments;
    private final MerchantViewQueryPort merchantViewQueryPort;

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

        Set<String> scopeIds = contexts.keySet().stream()
                .map(ContextKey::scopeId)
                .collect(Collectors.toSet());

        Map<String, MerchantView> viewsByScopeId = merchantViewQueryPort.findAllByScopeIdIn(scopeIds).stream()
                .collect(Collectors.toMap(MerchantView::getScopeId, Function.identity()));

        return contexts.values().stream()
                .map(contextAssignments -> toResult(contextAssignments, viewsByScopeId))
                .toList();
    }

    private AccessContextResult toResult(
            List<AccessAssignmentView> contextAssignments,
            Map<String, MerchantView> viewsByScopeId
    ) {
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

        MerchantView view = viewsByScopeId.get(anchor.scopeId());
        AccessContextResult.DisplayContext displayContext = view == null ? null : new AccessContextResult.DisplayContext(
                view.getName(),
                view.getStatus()
        );

        return new AccessContextResult(
                anchor.id(),
                anchor.platformCode(),
                Set.copyOf(roleCodes),
                anchor.scopeKey(),
                anchor.scopeId(),
                expiresAt,
                displayContext
        );
    }

    private record ContextKey(String scopeKey, String scopeId) {
    }
}
