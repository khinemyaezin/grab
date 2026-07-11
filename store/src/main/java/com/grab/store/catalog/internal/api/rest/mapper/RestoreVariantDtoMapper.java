package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import com.grab.store.catalog.internal.command.RestoreVariantCommand;
import com.grab.store.catalog.internal.command.RestoreVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RestoreVariantDtoMapper {

    public abstract RestoreVariantCommand toCommand(String merchantId, String productId, String variantId);

    public abstract RestoreVariantResponse toResponse(RestoreVariantResult result);
}
