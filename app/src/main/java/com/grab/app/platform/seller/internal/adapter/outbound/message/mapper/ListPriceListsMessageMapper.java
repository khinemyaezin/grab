package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceListPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceEntryOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListRuleOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
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

