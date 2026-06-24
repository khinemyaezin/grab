package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.SearchRolesQuery;
import com.grab.store.identity.internal.query.SearchRolesResult;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import com.identity.infrastructure.repository.jpa.RoleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchRolesQueryHandler implements QueryHandler<SearchRolesQuery, SearchRolesResult> {

    private final RoleQueryRepository roleRepository;

    @Override
    @IdentityReadTransactional
    public SearchRolesResult handle(SearchRolesQuery query) {
        return roleRepository.queryByName(query.name())
                .stream()
                .map(view -> new SearchRolesResult.Role(view.getId(), view.getName(), view.getCode()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), SearchRolesResult::new));
    }

    @Override
    public Class<SearchRolesQuery> getQueryType() {
        return SearchRolesQuery.class;
    }
}
