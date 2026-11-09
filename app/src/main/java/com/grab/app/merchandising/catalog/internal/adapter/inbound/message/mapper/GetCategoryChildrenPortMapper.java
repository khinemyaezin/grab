package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CategoryChildrenResult;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryChildrenQuery;
import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort.CategoryMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryChildrenPortMapper {

    public GetCategoryChildrenQuery toQuery(Id categoryId) {
        return categoryId != null ? new GetCategoryChildrenQuery(categoryId.getValue()) : null;
    }

    public List<CategoryMessageResult> toResponse(CategoryChildrenResult result) {
        if (result == null || result.children() == null) {
            return Collections.emptyList();
        }
        return result.children().stream().map(this::toItemResponse).toList();
    }

    public abstract CategoryMessageResult toItemResponse(CategoryResult result);
}

