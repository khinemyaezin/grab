package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.query.ListUsersQuery;
import com.grab.store.identity.internal.query.ListUsersResult;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListUsersQueryHandler implements QueryHandler<ListUsersQuery, Page<ListUsersResult>> {

    private final UserJpaRepository userRepository;

    @Override
    @IdentityReadTransactional
    public Page<ListUsersResult> handle(ListUsersQuery query) {
        return userRepository.findAll(query.pageable()).map(user -> new ListUsersResult(
                user.getUuid(),
                user.getEmail(),
                user.getStatus().name(),
                user.getCreatedAt().toString()
        ));
    }

    @Override
    public Class<ListUsersQuery> getQueryType() {
        return ListUsersQuery.class;
    }
}
