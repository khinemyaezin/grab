package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.command.CreateProductSetCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SaveProductDtoMapper {

    public abstract CreateProductSetCommand toCommand(SaveProductRequest request, String merchantId);

    protected abstract CreateProductSetCommand.Product toCommandProduct(SaveProductRequest.Product product);

    protected abstract CreateProductSetCommand.Variant toCommandVariant(SaveProductRequest.Variant variant);

    protected abstract CreateProductSetCommand.Variation toCommandVariation(SaveProductRequest.Variation variation);
}
