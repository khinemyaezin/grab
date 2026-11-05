package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerMerchantMembersQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerMerchantMembersMapper {

    public abstract ListSellerMerchantMembersQuery toQuery(String merchantId);

    public abstract List<MerchantMemberResponse> toResponse(List<MerchantMemberOutboundResult> results);

    public abstract MerchantMemberResponse toResponse(MerchantMemberOutboundResult result);
}

