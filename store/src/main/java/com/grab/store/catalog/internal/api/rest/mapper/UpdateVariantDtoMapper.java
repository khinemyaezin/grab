package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateVariantDtoMapper {
    public abstract UpdateVariantCommand toCommand(String merchantId, String productId, String variantId, UpdateVariantRequest request);

    public abstract UpdateVariantResponse toResponse(UpdateVariantResult result);
}
