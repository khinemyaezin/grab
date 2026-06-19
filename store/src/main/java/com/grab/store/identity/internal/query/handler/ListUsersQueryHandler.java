package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.query.ListUsersQuery;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListUsersQueryHandler implements QueryHandler<ListUsersQuery, Page<UserProfileResult>> {
    private final UserJpaRepository users;

    @Override
    @IdentityReadTransactional
    public Page<UserProfileResult> handle(ListUsersQuery query) {
        return users.findAll(query.pageable()).map(u -> 
            new UserProfileResult(u.getUuid(), u.getEmail(), u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()), u.getStatus().name(), u.getCreatedAt())
        );
    }

    @Override
    public Class<ListUsersQuery> getQueryType() {
        return ListUsersQuery.class;
    }
}
