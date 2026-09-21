package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.store.pricing.internal.api.rest.dto.request.CreatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreatePriceListRequestMapper {

    public abstract CreatePriceListCommand toCommand(CreatePriceListRequest request);

    public abstract PriceListResponse toResponse(PriceListResult result);
}
