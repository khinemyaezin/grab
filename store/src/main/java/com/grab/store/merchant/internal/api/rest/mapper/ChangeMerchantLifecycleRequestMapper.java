package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand.Action;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeMerchantLifecycleRequestMapper {
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "reason", source = "request.reason")
    public abstract ChangeMerchantLifecycleCommand toCommand(
            String merchantId, String actorId, Action action, MerchantLifecycleRequest request);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
