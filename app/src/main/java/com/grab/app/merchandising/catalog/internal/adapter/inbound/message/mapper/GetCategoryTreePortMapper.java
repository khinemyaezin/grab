package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CategoryNodeResult;
import com.catalog.application.model.read.GetCategoryTreeQuery;
import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort.CategoryTreeNodeResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryTreePortMapper {

    public GetCategoryTreeQuery toQuery(Id rootCategoryId) {
        return new GetCategoryTreeQuery(rootCategoryId != null ? rootCategoryId.getValue() : null);
    }

    public CategoryTreeNodeResult toResponse(CategoryNodeResult result) {
        if (result == null) {
            return null;
        }
        List<CategoryTreeNodeResult> children = new ArrayList<>();
        if (result.children() != null) {
            for (CategoryNodeResult child : result.children()) {
                children.add(toResponse(child));
            }
        }
        return new CategoryTreeNodeResult(result.id(), result.name(), result.parentId(), children);
    }
}

