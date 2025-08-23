package com.grab.store.product.internal.api.rest.mapper;

import com.grab.framework.id.Id;
import com.grab.store.product.internal.api.rest.dto.CreateProductRequest;
import com.grab.store.product.internal.command.CreateProductCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = CreateCommandMapper.class)
public interface CreateProductCommandMapper {
    CreateProductCommand map(CreateProductRequest createProductRequest);

    CreateProductCommand.VariantCommand variantToVariantCommand(CreateProductRequest.Variant variant);
}
