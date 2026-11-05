package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerMerchantQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerMerchantMapper {

    public abstract GetSellerMerchantQuery toQuery(String merchantId, String actorId);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}

