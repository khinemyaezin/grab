package com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceSetPort.CreatePricePreferenceMessage;
import com.grab.service.contract.message.merchandising.pricing.PriceSetPort.PricePreferenceMessageResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePricePreferencePortMapper {

    public abstract CreatePricePreferenceCommand toCommand(CreatePricePreferenceMessage message);

    public abstract PricePreferenceMessageResult toResponse(PricePreferenceResult result);
}

