package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;
import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort.CategoryMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCategoryLeavesPortMapper {

    public GetCategoryLeafNodesByNameQuery toQuery(String name) {
        return new GetCategoryLeafNodesByNameQuery(name);
    }

    public List<CategoryMessageResult> toResponse(CategoryLeavesResult result) {
        if (result == null || result.leaves() == null) {
            return Collections.emptyList();
        }
        return result.leaves().stream().map(this::toItemResponse).toList();
    }

    public abstract CategoryMessageResult toItemResponse(CategoryResult result);
}

