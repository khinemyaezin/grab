package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListUsersRequestMapper {
    public abstract ListUsersQuery toQuery(Pageable pageable);

    public abstract UserProfileResponse toResponse(ListUsersResult result);
}
