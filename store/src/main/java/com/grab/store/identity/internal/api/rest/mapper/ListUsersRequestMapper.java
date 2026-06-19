package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.query.ListUsersQuery;
import com.grab.store.identity.internal.query.ListUsersResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListUsersRequestMapper {
    public abstract ListUsersQuery toQuery(Pageable pageable);

    public abstract UserProfileResponse toResponse(ListUsersResult result);
}
