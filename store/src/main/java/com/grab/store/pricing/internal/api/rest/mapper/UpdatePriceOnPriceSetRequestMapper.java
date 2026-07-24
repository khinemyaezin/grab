package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.command.UpdatePriceOnPriceSetCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdatePriceOnPriceSetRequestMapper {

    @Mapping(target = "priceSetId", source = "priceSetId")
    @Mapping(target = "priceId", source = "priceId")
    public abstract UpdatePriceOnPriceSetCommand toCommand(
            String priceSetId,
            String priceId,
            UpdatePriceRequest request
    );

    public abstract PriceSetResponse toResponse(PriceSetResult result);
}
