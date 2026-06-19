package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListRolesRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final QueryBus queryBus;
    private final ListRolesRequestMapper listRolesMapper;

    public Page<RoleResponse> listRoles(Pageable pageable) {
        return queryBus.dispatch(listRolesMapper.toQuery(pageable)).map(listRolesMapper::toResponse);
    }
}
