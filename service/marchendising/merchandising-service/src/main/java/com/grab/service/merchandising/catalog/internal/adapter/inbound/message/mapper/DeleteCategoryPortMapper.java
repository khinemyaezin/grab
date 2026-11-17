package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.DeleteCategoryCommand;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteCategoryPortMapper {

    public DeleteCategoryCommand toCommand(Id categoryId) {
        return new DeleteCategoryCommand(categoryId);
    }
}

