package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, UserProfileResult> {
    private final UserJpaRepository users;

    @Override
    @IdentityReadTransactional
    public UserProfileResult handle(GetUserProfileQuery query) {
        UserEntity u = users.findByUuid(query.userId())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.UserNotFound(query.userId()), "User not found"));
        return new UserProfileResult(u.getUuid(), u.getEmail(), u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()), u.getStatus().name(), u.getCreatedAt());
    }

    @Override
    public Class<GetUserProfileQuery> getQueryType() {
        return GetUserProfileQuery.class;
    }
}
