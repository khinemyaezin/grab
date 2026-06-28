package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AccessContextResponse;
import com.grab.store.identity.internal.query.AccessContextResult;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListAccessContextsRequestMapper {
    public abstract ListAccessContextsQuery toQuery(String userId, String platformCode);

    public abstract AccessContextResponse toResponse(AccessContextResult result);
}
