package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SaveProductDtoMapper {

    public abstract SaveProductCommand toCommand(SaveProductRequest request);

    protected abstract SaveProductCommand.Product toCommandProduct(SaveProductRequest.Product product);

    protected abstract SaveProductCommand.Variant toCommandVariant(SaveProductRequest.Variant variant);

    protected abstract SaveProductCommand.Variation toCommandVariation(SaveProductRequest.Variation variation);
}
