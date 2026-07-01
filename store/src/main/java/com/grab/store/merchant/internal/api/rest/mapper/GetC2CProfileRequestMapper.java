package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.GetC2CApplicationResponse;
import com.grab.store.merchant.internal.query.GetC2CApplicationQuery;
import com.grab.store.merchant.internal.query.GetC2CApplicationResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetC2CProfileRequestMapper {
    public abstract GetC2CApplicationQuery toQuery(String applicantUserId);
    public abstract GetC2CApplicationResponse toResponse(GetC2CApplicationResult view);
}
