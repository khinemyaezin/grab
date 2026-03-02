package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.grab.store.catalog.internal.command.SaveCategoryCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdConverter.class)
public abstract class SaveCategoryDtoMapper {
    public abstract SaveCategoryCommand toCommand(SaveCategoryRequest request);
}
