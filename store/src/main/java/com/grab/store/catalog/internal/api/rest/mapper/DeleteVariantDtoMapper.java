package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.command.DeleteVariantCommand;
import com.grab.store.catalog.internal.command.DeleteVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteVariantDtoMapper {

    public abstract DeleteVariantCommand toCommand(String merchantId, String productId, String variantId);

    public abstract DeleteVariantResponse toResponse(DeleteVariantResult result);
}
