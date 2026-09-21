package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand.Action;
import com.merchant.application.model.write.MerchantAccountResult;
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
