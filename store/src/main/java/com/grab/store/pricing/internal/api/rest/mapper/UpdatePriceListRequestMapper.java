package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.UpdatePriceListCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdatePriceListRequestMapper {

    @Mapping(target = "priceListId", source = "priceListId")
    public abstract UpdatePriceListCommand toCommand(String priceListId, UpdatePriceListRequest request);

    public abstract PriceListResponse toResponse(PriceListResult result);
}
