package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort.PriceSetMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePriceSetPortMapper {

    public CreatePriceSetCommand toCommand() {
        return new CreatePriceSetCommand();
    }

    @Mapping(target = "id", source = "priceSetId")
    @Mapping(target = "prices", expression = "java(java.util.List.of())")
    public abstract PriceSetMessageResult toResponse(CreatePriceSetResult result);
}

