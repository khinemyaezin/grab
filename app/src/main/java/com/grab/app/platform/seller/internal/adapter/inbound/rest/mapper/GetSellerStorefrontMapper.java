package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerStorefrontQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerStorefrontMapper {

    public abstract GetSellerStorefrontQuery toQuery(String merchantId, String storefrontId);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

