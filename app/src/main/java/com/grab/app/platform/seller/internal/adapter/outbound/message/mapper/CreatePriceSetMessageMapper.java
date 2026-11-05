package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceEntryOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePriceSetMessageMapper {

    public abstract PriceSetOutboundResult toResult(PriceSetPort.PriceSetMessageResult result);

    public abstract PriceEntryOutboundResult toEntryResult(PriceSetPort.PriceEntryMessageResult result);
}

