package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.query.ListRolesQuery;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListRolesQueryHandler implements QueryHandler<ListRolesQuery, List<RoleResult>> {
    private final RoleJpaRepository roles;

    @Override
    @IdentityReadTransactional
    public List<RoleResult> handle(ListRolesQuery query) {
        return roles.findAll().stream().map(r -> 
            new RoleResult(r.getCode(), r.getName(), r.getDescription(), r.isActive(), r.getAuthorities().stream().map(AuthorityEntity::getCode).collect(Collectors.toSet()))
        ).toList();
    }

    @Override
    public Class<ListRolesQuery> getQueryType() {
        return ListRolesQuery.class;
    }
}
