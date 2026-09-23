package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.GetUserProfileRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.ListUsersRequestMapper;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
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
        GetUserProfileQuery query = getUserProfileMapper.toQuery(id);
        GetUserProfileResult result = queryBus.dispatch(query);
        return getUserProfileMapper.toResponse(result);
    }

    public Page<UserProfileResponse> listUsers(Pageable pageable) {
        ListUsersQuery query = listUsersMapper.toQuery(pageable);
        Page<ListUsersResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listUsersMapper::toResponse);
    }
}
