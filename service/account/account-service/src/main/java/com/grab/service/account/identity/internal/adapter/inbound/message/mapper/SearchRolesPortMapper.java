package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.RolePermissionPort.RoleSuggestionResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchRolesPortMapper {

    public SearchRolesQuery toQuery(String name) {
        return new SearchRolesQuery(name);
    }

    public abstract RoleSuggestionResult toResponse(SearchRolesResult.Role role);
}
