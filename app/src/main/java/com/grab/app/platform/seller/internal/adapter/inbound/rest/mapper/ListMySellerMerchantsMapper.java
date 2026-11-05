package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListMySellerMerchantsQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListMySellerMerchantsMapper {

    public abstract ListMySellerMerchantsQuery toQuery(String applicantUserId);

    public abstract List<MerchantResponse> toResponse(List<MerchantOutboundResult> results);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}

