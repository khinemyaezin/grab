package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.RolePermissionPort.RoleSuggestionResult;
import com.grab.app.shared.config.CentralMapperConfig;
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
