package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPriceListQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPriceListMapper {

    public abstract GetSellerPriceListQuery toQuery(String priceListId);

    public abstract PriceListResponse toResponse(PriceListOutboundResult result);
}

