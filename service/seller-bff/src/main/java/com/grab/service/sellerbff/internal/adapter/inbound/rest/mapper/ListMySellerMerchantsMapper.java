package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.ListMySellerMerchantsQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListMySellerMerchantsMapper {

    public abstract ListMySellerMerchantsQuery toQuery(String applicantUserId);

    public abstract List<MerchantResponse> toResponse(List<MerchantOutboundResult> results);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}

