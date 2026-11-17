package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.GetSellerStorefrontQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerStorefrontMapper {

    public abstract GetSellerStorefrontQuery toQuery(String merchantId, String storefrontId);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

