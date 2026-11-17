package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryQuery;
import com.grab.service.contract.message.merchandising.catalog.CatalogCategoryPort.CategoryMessageResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryPortMapper {

    public GetCategoryQuery toQuery(Id categoryId) {
        return categoryId != null ? new GetCategoryQuery(categoryId.getValue()) : null;
    }

    public abstract CategoryMessageResult toResponse(CategoryResult result);
}

