package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPriceListQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPriceListMapper {

    public abstract GetSellerPriceListQuery toQuery(String priceListId);

    public abstract PriceListResponse toResponse(PriceListOutboundResult result);
}

