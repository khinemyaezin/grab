package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductResponse;
import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductDtoMapper {

    public abstract UpdateProductCommand toCommand(String merchantId, String productId, UpdateProductRequest request);

    public abstract UpdateProductResponse toResponse(UpdateProductResult result);
}
