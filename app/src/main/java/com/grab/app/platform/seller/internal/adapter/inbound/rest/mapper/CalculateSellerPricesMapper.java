package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CalculatePriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CalculatedPriceResponse;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.CalculateSellerPricesQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class CalculateSellerPricesMapper {

    public abstract CalculateSellerPricesQuery toQuery(CalculatePriceRequest request);

    public abstract List<CalculatedPriceResponse> toResponse(List<CalculatedPriceOutboundResult> results);

    public abstract CalculatedPriceResponse toResponse(CalculatedPriceOutboundResult result);
}

