package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.ListRolesQuery;
import com.grab.store.identity.internal.query.ListRolesResult;
import com.identity.domain.repository.PlatformRepository;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ListRolesQueryHandler implements QueryHandler<ListRolesQuery, Page<ListRolesResult>> {

    private final RoleJpaRepository roleRepository;
    private final PlatformRepository platformRepository;

    @Override
    @IdentityReadTransactional
    public Page<ListRolesResult> handle(ListRolesQuery query) {
        return roleRepository.findAll(query.pageable()).map(role -> {
            Set<String> platformCodes = platformRepository.findByRoleCode(role.getCode()).stream()
                    .map(platform -> platform.getCode())
                    .collect(Collectors.toSet());
            Set<String> authorityCodes = role.getAuthorities().stream()
                    .map(AuthorityEntity::getCode)
                    .collect(Collectors.toSet());
            return new ListRolesResult(
                    role.getCode(),
                    role.getName(),
                    role.getDescription(),
                    role.getKind().name(),
                    role.isActive(),
                    role.isAssignable(),
                    authorityCodes,
                    platformCodes
            );
        });
    }

    @Override
    public Class<ListRolesQuery> getQueryType() {
        return ListRolesQuery.class;
    }
}
