package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CreatePricePreferenceRecord;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePricePreferenceMessageMapper {

    public abstract PriceSetPort.CreatePricePreferenceMessage toMessage(CreatePricePreferenceRecord record);

    public abstract PricePreferenceOutboundResult toResult(PriceSetPort.PricePreferenceMessageResult result);
}

