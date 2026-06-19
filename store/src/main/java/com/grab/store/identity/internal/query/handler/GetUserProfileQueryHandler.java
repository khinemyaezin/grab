package com.grab.store.identity.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUserProfileQueryHandler implements QueryHandler<GetUserProfileQuery, GetUserProfileResult> {

    private final UserJpaRepository userRepository;

    @Override
    @IdentityReadTransactional
    public GetUserProfileResult handle(GetUserProfileQuery query) {
        UserEntity user = userRepository.findByUuid(query.userId().getValue())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(query.userId().getValue()),
                        "User not found"
                ));
        return new GetUserProfileResult(
                user.getUuid(),
                user.getEmail(),
                user.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()),
                user.getStatus().name(),
                user.getCreatedAt().toString()
        );
    }

    @Override
    public Class<GetUserProfileQuery> getQueryType() {
        return GetUserProfileQuery.class;
    }
}
