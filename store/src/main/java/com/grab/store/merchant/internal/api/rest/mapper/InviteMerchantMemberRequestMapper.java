package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.domain.valueobject.MerchantRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InviteMerchantMemberRequestMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "targetUserId", source = "request.targetUserId")
    @Mapping(target = "role", expression = "java(toMerchantRole(request.role()))")
    @Mapping(target = "expiresAt", source = "expiresAt")
    public abstract InviteMerchantMemberCommand toCommand(
            String merchantId, String actorId, InviteMerchantMemberRequest request, Instant expiresAt
    );

    public abstract MerchantMemberResponse toResponse(MerchantMemberResult result);

    protected MerchantRole toMerchantRole(String role) {
        return role != null ? MerchantRole.of(role) : null;
    }
}
