package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantMembersRequestMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    public abstract ListMerchantMembersQuery toQuery(String merchantId);

    public abstract List<MerchantMemberResponse> toResponse(List<MerchantMemberResult> results);

    public abstract MerchantMemberResponse toResponse(MerchantMemberResult result);
}
