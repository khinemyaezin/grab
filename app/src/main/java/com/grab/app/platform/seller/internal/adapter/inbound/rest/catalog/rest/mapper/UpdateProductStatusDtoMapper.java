package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.UpdateProductStatusRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.UpdateProductStatusResponse;
import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductStatusDtoMapper {

    public abstract UpdateProductStatusCommand toCommand(String merchantId, String productId, UpdateProductStatusRequest request);

    public abstract UpdateProductStatusResponse toResponse(UpdateProductStatusResult result);
}
