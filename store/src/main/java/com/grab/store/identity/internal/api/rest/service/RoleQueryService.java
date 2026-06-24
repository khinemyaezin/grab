package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListRolesRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.SearchRolesRequestMapper;
import com.grab.store.identity.internal.api.rest.dto.response.SearchRolesResponse;
import com.grab.store.identity.internal.query.SearchRolesQuery;
import com.grab.store.identity.internal.query.SearchRolesResult;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final QueryBus queryBus;
    private final ListRolesRequestMapper listRolesMapper;
    private final SearchRolesRequestMapper searchRolesMapper;

    public Page<RoleResponse> listRoles(Pageable pageable) {
        return queryBus.dispatch(listRolesMapper.toQuery(pageable)).map(listRolesMapper::toResponse);
    }

    public List<SearchRolesResponse> searchRoles(String name) {
        SearchRolesQuery query = searchRolesMapper.toQuery(name);
        SearchRolesResult result = queryBus.dispatch(query);
        return searchRolesMapper.toResponse(result);
    }
}
