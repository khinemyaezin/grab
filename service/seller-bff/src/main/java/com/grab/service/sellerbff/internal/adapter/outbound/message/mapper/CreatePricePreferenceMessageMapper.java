package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceSetPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.CreatePricePreferenceRecord;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePricePreferenceMessageMapper {

    public abstract PriceSetPort.CreatePricePreferenceMessage toMessage(CreatePricePreferenceRecord record);

    public abstract PricePreferenceOutboundResult toResult(PriceSetPort.PricePreferenceMessageResult result);
}

