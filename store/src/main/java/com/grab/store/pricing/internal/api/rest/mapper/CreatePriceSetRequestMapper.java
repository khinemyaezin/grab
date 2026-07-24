package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.store.pricing.internal.command.CreatePriceSetCommand;
import com.grab.store.pricing.internal.command.CreatePriceSetResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreatePriceSetRequestMapper {

    public CreatePriceSetCommand toCommand() {
        return new CreatePriceSetCommand();
    }

    public String toResponse(CreatePriceSetResult result) {
        return result.priceSetId();
    }
}
