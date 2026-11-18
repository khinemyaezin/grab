package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.pricing.PriceListPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceEntryOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListRuleOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListPriceListsMessageMapper {

    public abstract List<PriceListOutboundResult> toResultList(List<PriceListPort.PriceListMessageResult> results);

    public abstract PriceListOutboundResult toResult(PriceListPort.PriceListMessageResult result);

    public abstract PriceListRuleOutboundResult toRuleResult(PriceListPort.PriceListRuleMessageResult result);

    public abstract PriceEntryOutboundResult toEntryResult(PriceListPort.PriceEntryMessageResult result);
}

