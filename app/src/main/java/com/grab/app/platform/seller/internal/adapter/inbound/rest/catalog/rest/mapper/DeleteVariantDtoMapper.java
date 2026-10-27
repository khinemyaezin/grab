package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.DeleteVariantResponse;
import com.catalog.application.model.write.DeleteVariantCommand;
import com.catalog.application.model.write.DeleteVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteVariantDtoMapper {

    public abstract DeleteVariantCommand toCommand(String merchantId, String productId, String variantId);

    public abstract DeleteVariantResponse toResponse(DeleteVariantResult result);
}
