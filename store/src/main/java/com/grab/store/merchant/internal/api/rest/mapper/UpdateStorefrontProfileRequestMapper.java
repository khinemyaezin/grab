package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.command.UpdateStorefrontProfileCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateStorefrontProfileRequestMapper {
    public abstract UpdateStorefrontProfileCommand toCommand(
            String storefrontId, String merchantId, UpdateStorefrontProfileRequest request);

    public abstract StorefrontResponse toResponse(StorefrontResult result);
}
