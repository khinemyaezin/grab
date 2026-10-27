package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.StorefrontResponse;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateStorefrontRequestMapper {
    public abstract CreateStorefrontCommand toCommand(String merchantId, CreateStorefrontRequest request);

    public abstract StorefrontResponse toResponse(StorefrontResult result);
}
