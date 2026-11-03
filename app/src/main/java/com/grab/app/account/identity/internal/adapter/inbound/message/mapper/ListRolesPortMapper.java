package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.RolePermissionPort.RoleItemResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageRequest;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListRolesPortMapper {

    public ListRolesQuery toQuery(String name, int page, int size) {
        return new ListRolesQuery(name, PageRequest.of(page, size));
    }

    public abstract RoleItemResult toResponse(ListRolesResult result);
}
