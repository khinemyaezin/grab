package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.pricing.application.model.write.AddPriceToPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AddPriceToPriceSetRequestMapper {

    @Mapping(target = "priceSetId", source = "priceSetId")
    public abstract AddPriceToPriceSetCommand toCommand(String priceSetId, AddPriceRequest request);

    public abstract PriceSetResponse toResponse(PriceSetResult result);
}
