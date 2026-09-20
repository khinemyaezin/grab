package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.read.GetPriceSetQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPriceSetRequestMapper {

    public abstract GetPriceSetQuery toQuery(String priceSetId);

    public abstract PriceSetResponse toResponse(PriceSetResult result);
}
