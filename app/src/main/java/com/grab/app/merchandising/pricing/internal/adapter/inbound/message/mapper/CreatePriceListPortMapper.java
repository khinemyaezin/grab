package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceListPort.CreatePriceListMessage;
import com.grab.app.merchandising.pricing.port.message.PriceListPort.PriceListMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePriceListPortMapper {

    public abstract CreatePriceListCommand toCommand(CreatePriceListMessage message);

    public abstract PriceListMessageResult toResponse(PriceListResult result);
}

