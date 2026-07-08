package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.AccessContextResult;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.infrastructure.repository.jpa.MerchantViewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.identity.infrastructure.view.MerchantView;

@Component
@RequiredArgsConstructor
public class ListAccessContextsQueryHandler
        implements QueryHandler<ListAccessContextsQuery, List<AccessContextResult>> {
    private final AccessAssignmentRepository assignments;
    private final MerchantViewJpaRepository merchantViewRepository;

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

        Set<String> scopeIds = contexts.keySet().stream()
                .map(ContextKey::scopeId)
                .collect(Collectors.toSet());

        Map<String, MerchantView> viewsByScopeId = merchantViewRepository.findAllByScopeIdIn(scopeIds).stream()
                .collect(Collectors.toMap(
                        MerchantView::getScopeId,
                        Function.identity()));

        return contexts.values().stream()
                .map(contextAssignments ->
                        toResult(contextAssignments, viewsByScopeId))
                .toList();
    }

    private AccessContextResult toResult(List<AccessAssignment> contextAssignments, Map<String, MerchantView> viewsByScopeId) {
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

        MerchantView view = viewsByScopeId.get(anchor.getScope().scopeId());
        AccessContextResult.DisplayContext displayContext = view == null ? null : new AccessContextResult.DisplayContext(
                view.getName(),
                view.getStatus()
        );

        return new AccessContextResult(
                anchor.getId().getValue(),
                anchor.getPlatformCode(),
                Set.copyOf(roleCodes),
                anchor.getScope().key().value(),
                anchor.getScope().scopeId(),
                expiresAt,
                displayContext
        );
    }

    @Override
    public Class<ListAccessContextsQuery> getQueryType() {
        return ListAccessContextsQuery.class;
    }

    private record ContextKey(String scopeKey, String scopeId) {
    }
}
