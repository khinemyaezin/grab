package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand.Action;
import com.merchant.application.model.write.StorefrontResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeStorefrontLifecycleRequestMapper {
    @Mapping(target = "storefrontId", source = "storefrontId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "reason", source = "reason")
    public abstract ChangeStorefrontLifecycleCommand toCommand(
            String storefrontId, String merchantId, Action action, String reason);

    public ChangeStorefrontLifecycleCommand toCommand(
            String storefrontId, String merchantId, Action action, MerchantLifecycleRequest request) {
        return toCommand(storefrontId, merchantId, action, request == null ? null : request.reason());
    }

    public abstract StorefrontResponse toResponse(StorefrontResult result);
}
