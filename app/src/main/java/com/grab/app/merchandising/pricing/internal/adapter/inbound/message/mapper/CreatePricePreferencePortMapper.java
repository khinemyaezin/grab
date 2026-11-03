package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort.CreatePricePreferenceMessage;
import com.grab.app.merchandising.pricing.port.message.PriceSetPort.PricePreferenceMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePricePreferencePortMapper {

    public abstract CreatePricePreferenceCommand toCommand(CreatePricePreferenceMessage message);

    public abstract PricePreferenceMessageResult toResponse(PricePreferenceResult result);
}

