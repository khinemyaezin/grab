package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.UpdateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.UpdateProductResponse;
import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductDtoMapper {

    public abstract UpdateProductCommand toCommand(String merchantId, String productId, UpdateProductRequest request);

    public abstract UpdateProductResponse toResponse(UpdateProductResult result);
}
