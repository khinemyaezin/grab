package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.StorefrontResponse;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateStorefrontProfileRequestMapper {
    public abstract UpdateStorefrontProfileCommand toCommand(
            String storefrontId, String merchantId, UpdateStorefrontProfileRequest request);

    public abstract StorefrontResponse toResponse(StorefrontResult result);
}
