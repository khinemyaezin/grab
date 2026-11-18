package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceSetPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceEntryOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPriceSetMessageMapper {

    public abstract PriceSetOutboundResult toResult(PriceSetPort.PriceSetMessageResult result);

    public abstract PriceEntryOutboundResult toEntryResult(PriceSetPort.PriceEntryMessageResult result);
}

