package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.CalculatePriceRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CalculatedPriceResponse;
import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.CalculateSellerPricesQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class CalculateSellerPricesMapper {

    @Mapping(target = "priceSetIds", source = "priceSetIds")
    @Mapping(target = "currencyCode", expression = "java(request.resolveCurrencyCode())")
    @Mapping(target = "quantity", expression = "java(request.resolveQuantity())")
    @Mapping(target = "attributes", expression = "java(request.resolveAttributes())")
    public abstract CalculateSellerPricesQuery toQuery(CalculatePriceRequest request);

    public abstract List<CalculatedPriceResponse> toResponse(List<CalculatedPriceOutboundResult> results);

    public abstract CalculatedPriceResponse toResponse(CalculatedPriceOutboundResult result);
}

