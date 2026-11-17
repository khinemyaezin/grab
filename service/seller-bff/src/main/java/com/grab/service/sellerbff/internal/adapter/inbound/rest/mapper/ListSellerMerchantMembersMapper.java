package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.ListSellerMerchantMembersQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerMerchantMembersMapper {

    public abstract ListSellerMerchantMembersQuery toQuery(String merchantId);

    public abstract List<MerchantMemberResponse> toResponse(List<MerchantMemberOutboundResult> results);

    public abstract MerchantMemberResponse toResponse(MerchantMemberOutboundResult result);
}

