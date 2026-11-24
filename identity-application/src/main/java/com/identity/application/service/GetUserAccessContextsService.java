package com.identity.application.service;

import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.UserAccessContextsResult;
import com.identity.application.model.read.UserAccessContextsResult.AccessContextView;
import com.identity.application.port.inbound.GetUserAccessContextsUseCase;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.RoleRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetUserAccessContextsService implements GetUserAccessContextsUseCase {
    private final ScopeCatalogQueryPort scopes;
    private final AccessAssignmentQueryPort assignments;
    private final RoleRepository roles;

    @Override
    public UserAccessContextsResult execute(GetUserAccessContextsQuery query) {
        if (query.userId() == null) {
            return new UserAccessContextsResult(null, List.of());
        }
        var catalog = scopes.load();
        var hierarchy = catalog.hierarchy();
        Instant now = Instant.now();
        List<AccessAssignmentView> views = assignments.findEffectiveByUser(query.userId().getValue(), now);

        Map<ContextKey, List<AccessAssignmentView>> grouped = views.stream()
                .filter(assignment -> hierarchy.isEffective(assignment.scopeKey()))
                .collect(Collectors.groupingBy(
                        a -> new ContextKey(a.scopeKey(), a.scopeId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<AccessContextView> contextViews = new ArrayList<>();
        for (List<AccessAssignmentView> contextAssignments : grouped.values()) {
            AccessAssignmentView anchor = contextAssignments.getFirst();
            Set<String> roleCodes = contextAssignments.stream()
                    .map(AccessAssignmentView::roleCode)
                    .collect(Collectors.toSet());

            Set<Role> roleAggregates = roles.findByCodes(roleCodes);
            Set<String> authorities = roleAggregates.stream()
                    .filter(Role::isActive)
                    .flatMap(r -> r.getAuthorities().stream())
                    .map(Authority::getCode)
                    .collect(Collectors.toCollection(TreeSet::new));

            String displayName = anchor.title() != null && !anchor.title().isBlank()
                    ? anchor.title()
                    : anchor.scopeKey() + ":" + anchor.scopeId();

            contextViews.add(new AccessContextView(
                    anchor.id(),
                    anchor.scopeKey(),
                    anchor.scopeId(),
                    anchor.roleCode(),
                    displayName,
                    authorities
            ));
        }

        return new UserAccessContextsResult(query.userId(), contextViews);
    }

    private record ContextKey(String scopeKey, String scopeId) {}
}
