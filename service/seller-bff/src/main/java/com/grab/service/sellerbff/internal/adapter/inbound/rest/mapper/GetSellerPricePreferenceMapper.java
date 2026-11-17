package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPricePreferenceQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPricePreferenceMapper {

    public abstract GetSellerPricePreferenceQuery toQuery(String preferenceId);

    public abstract PricePreferenceResponse toResponse(PricePreferenceOutboundResult result);
}

