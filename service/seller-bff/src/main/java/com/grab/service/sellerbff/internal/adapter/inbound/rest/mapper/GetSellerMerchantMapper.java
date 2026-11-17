package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.GetSellerMerchantQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerMerchantMapper {

    public abstract GetSellerMerchantQuery toQuery(String merchantId, String actorId);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}

