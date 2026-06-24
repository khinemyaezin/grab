package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.SearchRolesResponse;
import com.grab.store.identity.internal.query.SearchRolesQuery;
import com.grab.store.identity.internal.query.SearchRolesResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchRolesRequestMapper {
    public abstract SearchRolesQuery toQuery(String name);

    public List<SearchRolesResponse> toResponse(SearchRolesResult result) {
        if (result == null) {
            return null;
        }
        return toResponseList(result.roles());
    }
    protected abstract List<SearchRolesResponse> toResponseList(List<SearchRolesResult.Role> roles);

    protected abstract SearchRolesResponse toResponseItem(SearchRolesResult.Role role);

}
