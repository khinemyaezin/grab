package com.grab.store.pricing.internal.api.rest.mapper;

import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
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
