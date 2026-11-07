package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatePriceRecord;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CalculatePricesMessageMapper {

    @Mapping(target = "priceSetIds", source = "priceSetIds")
    @Mapping(target = "currencyCode", source = "record.currencyCode")
    @Mapping(target = "quantity", source = "record.quantity")
    @Mapping(target = "attributes", source = "record.attributes")
    public abstract PriceSetPort.CalculatePriceMessage toMessage(CalculatePriceRecord record, List<Id> priceSetIds);

    public abstract List<CalculatedPriceOutboundResult> toResultList(List<PriceSetPort.CalculatedPriceMessageResult> results);

    public abstract CalculatedPriceOutboundResult toResult(PriceSetPort.CalculatedPriceMessageResult result);
}

