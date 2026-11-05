package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPricePreferenceQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerPricePreferenceMapper {

    public abstract GetSellerPricePreferenceQuery toQuery(String preferenceId);

    public abstract PricePreferenceResponse toResponse(PricePreferenceOutboundResult result);
}

