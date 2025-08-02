package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.UpdateProductRequest;
import com.grab.store.product.internal.command.UpdateProductCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface UpdateProductCommandMapper {
    UpdateProductCommand map(UpdateProductRequest updateProductRequest);
}
