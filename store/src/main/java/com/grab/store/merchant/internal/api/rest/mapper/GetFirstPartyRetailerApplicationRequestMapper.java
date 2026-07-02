package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.store.merchant.internal.query.GetFirstPartyRetailerApplicationQuery;
import com.grab.store.merchant.internal.query.GetFirstPartyRetailerApplicationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFirstPartyRetailerApplicationRequestMapper {
    public abstract GetFirstPartyRetailerApplicationQuery toQuery(String applicantUserId);
    public abstract GetFirstPartyRetailerApplicationResponse toResponse(GetFirstPartyRetailerApplicationResult result);
}
