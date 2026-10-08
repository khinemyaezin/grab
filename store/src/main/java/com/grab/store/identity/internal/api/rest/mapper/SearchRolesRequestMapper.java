package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.RoleSearchResponse;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchRolesRequestMapper {
    public abstract SearchRolesQuery toQuery(String name);

    public List<RoleSearchResponse> toResponse(SearchRolesResult result) {
        if (result == null) {
            return null;
        }
        return toResponseList(result.roles());
    }

    protected abstract List<RoleSearchResponse> toResponseList(List<SearchRolesResult.Role> roles);

    protected abstract RoleSearchResponse toResponseItem(SearchRolesResult.Role role);
}
