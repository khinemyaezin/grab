package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceListPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.command.AddPriceToPriceListCommand;
import com.grab.store.pricing.internal.command.PriceListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AddPriceToPriceListRequestMapper {

    @Mapping(target = "priceListId", source = "priceListId")
    public abstract AddPriceToPriceListCommand toCommand(
            String priceListId,
            AddPriceListPriceRequest request
    );

    public abstract PriceListResponse toResponse(PriceListResult result);
}
