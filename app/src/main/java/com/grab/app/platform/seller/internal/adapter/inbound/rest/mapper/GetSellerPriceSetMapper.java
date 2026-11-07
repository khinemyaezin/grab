package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPriceSetQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPriceSetMapper {

    public abstract GetSellerPriceSetQuery toQuery(String priceSetId);

    public abstract PriceSetResponse toResponse(PriceSetOutboundResult result);
}

