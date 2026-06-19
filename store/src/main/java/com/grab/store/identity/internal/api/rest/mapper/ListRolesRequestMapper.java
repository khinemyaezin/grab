package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.query.ListRolesQuery;
import com.grab.store.identity.internal.query.ListRolesResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListRolesRequestMapper {
    public abstract ListRolesQuery toQuery(Pageable pageable);

    public abstract RoleResponse toResponse(ListRolesResult result);
}
