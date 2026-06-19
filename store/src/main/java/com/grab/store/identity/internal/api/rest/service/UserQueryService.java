package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.GetUserProfileRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.ListUsersRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final QueryBus queryBus;
    private final GetUserProfileRequestMapper getUserProfileMapper;
    private final ListUsersRequestMapper listUsersMapper;

    public UserProfileResponse getUser(String id) {
        return getUserProfileMapper.toResponse(queryBus.dispatch(getUserProfileMapper.toQuery(id)));
    }

    public Page<UserProfileResponse> listUsers(Pageable pageable) {
        return queryBus.dispatch(listUsersMapper.toQuery(pageable)).map(listUsersMapper::toResponse);
    }
}
