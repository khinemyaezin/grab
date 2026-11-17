package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;
import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort.CategoryMessageResult;
import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort.CreateCategoryMessage;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SaveCategoryPortMapper {

    public SaveCategoryCommand toCommand(CreateCategoryMessage message) {
        if (message == null) {
            return null;
        }
        return new SaveCategoryCommand(
                message.name(),
                message.parentId(),
                message.active(),
                message.listingAllowed(),
                message.c2cAllowed()
        );
    }

    public CategoryMessageResult toResponse(SaveCategoryResult result, CreateCategoryMessage message) {
        if (result == null) {
            return null;
        }
        return new CategoryMessageResult(
                result.categoryId(),
                message != null ? message.name() : null,
                message != null && message.parentId() != null ? message.parentId().getValue() : null,
                message != null && Boolean.TRUE.equals(message.active()),
                message != null && Boolean.TRUE.equals(message.listingAllowed()),
                message != null && Boolean.TRUE.equals(message.c2cAllowed())
        );
    }
}

