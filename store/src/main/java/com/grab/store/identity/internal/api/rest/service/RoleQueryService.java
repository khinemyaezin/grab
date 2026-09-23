package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.dto.response.RoleSearchResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListRolesRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.SearchRolesRequestMapper;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
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
        ListRolesQuery query = listRolesMapper.toQuery(pageable);
        Page<ListRolesResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listRolesMapper::toResponse);
    }

    public List<RoleSearchResponse> searchRoles(String name) {
        SearchRolesQuery query = searchRolesMapper.toQuery(name);
        SearchRolesResult result = queryBus.dispatch(query);
        return searchRolesMapper.toResponse(result);
    }
}
