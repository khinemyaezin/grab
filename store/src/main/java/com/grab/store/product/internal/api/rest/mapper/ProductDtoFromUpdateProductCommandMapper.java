package com.grab.store.product.internal.api.rest.mapper;

import com.grab.store.product.internal.api.rest.dto.ProductDto;
import com.grab.store.product.internal.command.CreateProductCommand;
import com.grab.store.product.internal.command.UpdateProductCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface ProductDtoFromUpdateProductCommandMapper {
    ProductDto map(UpdateProductCommand updateProductCommand);
}
