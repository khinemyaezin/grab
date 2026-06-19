package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListRolesMapper;
import com.grab.store.identity.internal.api.rest.mapper.ListUsersMapper;
import com.grab.store.identity.internal.api.rest.mapper.UserProfileMapper;
import com.grab.store.identity.internal.api.rest.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityAdminQueryService {
    private final QueryBus bus;
    private final UserProfileMapper userProfileMapper;
    private final ListUsersMapper listUsersMapper;
    private final ListRolesMapper listRolesMapper;
    private final RoleMapper roleMapper;

    public UserProfileResponse profile(String id) {
        return userProfileMapper.toResponse(bus.dispatch(userProfileMapper.toQuery(id)));
    }

    public Page<UserProfileResponse> users(Pageable pageable) {
        return bus.dispatch(listUsersMapper.toQuery(pageable)).map(userProfileMapper::toResponse);
    }

    public List<RoleResponse> roles() {
        return bus.dispatch(listRolesMapper.toQuery()).stream().map(roleMapper::toResponse).toList();
    }
}
