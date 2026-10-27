package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFirstPartyRetailerApplicationRequestMapper {
    public abstract GetFirstPartyRetailerApplicationQuery toQuery(String applicantUserId);
    public abstract GetFirstPartyRetailerApplicationResponse toResponse(GetFirstPartyRetailerApplicationResult result);
}
