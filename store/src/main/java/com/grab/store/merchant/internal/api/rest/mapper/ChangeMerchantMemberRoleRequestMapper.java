package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.valueobject.MerchantRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeMerchantMemberRoleRequestMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "newRole", expression = "java(toMerchantRole(request.role()))")
    public abstract ChangeMerchantMemberRoleCommand toCommand(
            String merchantId, String memberId, String actorId, ChangeMerchantMemberRoleRequest request
    );

    public abstract MerchantMemberResponse toResponse(MerchantMemberResult result);

    protected MerchantRole toMerchantRole(String role) {
        return role != null ? MerchantRole.of(role) : null;
    }
}
