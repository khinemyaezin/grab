package com.grab.app.productmerchandising.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.catalog.application.model.write.SaveCategoryCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SaveCategoryDtoMapper {
    public abstract SaveCategoryCommand toCommand(SaveCategoryRequest request);
}
