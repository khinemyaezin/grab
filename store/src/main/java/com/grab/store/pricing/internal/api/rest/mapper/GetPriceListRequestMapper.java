package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.read.GetPriceListQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPriceListRequestMapper {

    public abstract GetPriceListQuery toQuery(String priceListId);

    public abstract PriceListResponse toResponse(PriceListResult result);
}
