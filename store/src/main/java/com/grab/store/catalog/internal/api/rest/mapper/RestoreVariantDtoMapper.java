package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import com.grab.store.catalog.internal.command.RestoreVariantCommand;
import com.grab.store.catalog.internal.command.RestoreVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class RestoreVariantDtoMapper {

    public abstract RestoreVariantCommand toCommand(String productId, String variantId);

    public abstract RestoreVariantResponse toResponse(RestoreVariantResult result);
}
