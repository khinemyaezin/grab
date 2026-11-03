package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetProductBySlugQuery;
import com.catalog.application.model.read.GetProductBySlugResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.VariantResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetProductBySlugPortMapper {

    public GetProductBySlugQuery toQuery(String slug) {
        return slug != null ? new GetProductBySlugQuery(slug) : null;
    }

    @Mapping(target = "categoryName", ignore = true)
    public abstract ProductResult toResponse(GetProductBySlugResult result);

    @Mapping(target = "matrixKey", ignore = true)
    protected abstract VariantResult toVariantResponse(GetProductBySlugResult.Variant variant);
}

