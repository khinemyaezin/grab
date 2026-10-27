package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.UpdateVariantRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.UpdateVariantResponse;
import com.catalog.application.model.write.UpdateVariantCommand;
import com.catalog.application.model.write.UpdateVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateVariantDtoMapper {
    public abstract UpdateVariantCommand toCommand(String merchantId, String productId, String variantId, UpdateVariantRequest request);

    public abstract UpdateVariantResponse toResponse(UpdateVariantResult result);
}
