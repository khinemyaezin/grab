package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.ListRolesQuery;
import com.grab.store.identity.internal.query.ListRolesResult;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListRolesQueryHandler implements QueryHandler<ListRolesQuery, Page<ListRolesResult>> {

    private final RoleJpaRepository roleRepository;

    @Override
    @IdentityReadTransactional
    public Page<ListRolesResult> handle(ListRolesQuery query) {
        return roleRepository.findAll(query.pageable()).map(role -> new ListRolesResult(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getAuthorities().stream().map(AuthorityEntity::getCode).collect(Collectors.toSet())
        ));
    }

    @Override
    public Class<ListRolesQuery> getQueryType() {
        return ListRolesQuery.class;
    }
}
