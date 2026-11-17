package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceSetPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPricePreferenceMessageMapper {

    public abstract PricePreferenceOutboundResult toResult(PriceSetPort.PricePreferenceMessageResult result);
}

