package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPriceSetQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPriceSetMapper {

    public abstract GetSellerPriceSetQuery toQuery(String priceSetId);

    public abstract PriceSetResponse toResponse(PriceSetOutboundResult result);
}

