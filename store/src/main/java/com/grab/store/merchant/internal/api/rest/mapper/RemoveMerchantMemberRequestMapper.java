package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RemoveMerchantMemberRequestMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "actorId", source = "actorId")
    public abstract RemoveMerchantMemberCommand toCommand(
            String merchantId, String memberId, String actorId
    );

    public abstract MerchantMemberResponse toResponse(MerchantMemberResult result);
}
