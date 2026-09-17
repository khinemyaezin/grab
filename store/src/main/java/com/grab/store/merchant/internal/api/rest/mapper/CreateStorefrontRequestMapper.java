package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.CreateStorefrontRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.command.CreateStorefrontCommand;
import com.grab.store.merchant.internal.command.StorefrontResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateStorefrontRequestMapper {
    public abstract CreateStorefrontCommand toCommand(String merchantId, CreateStorefrontRequest request);

    public abstract StorefrontResponse toResponse(StorefrontResult result);
}
