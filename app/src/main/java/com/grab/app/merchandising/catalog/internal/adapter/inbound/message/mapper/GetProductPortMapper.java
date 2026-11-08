package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.grab.app.merchandising.catalog.port.message.CatalogProductPort.ProductResult;
import com.grab.app.merchandising.catalog.port.message.CatalogProductPort.VariantResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetProductPortMapper {

    public GetProductQuery toQuery(Id merchantId, Id productId) {
        if (merchantId == null || productId == null) {
            return null;
        }
        return new GetProductQuery(merchantId.getValue(), productId.getValue());
    }

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    public abstract ProductResult toResponse(GetProductResult result);

    @Mapping(target = "status", source = "status")
    protected abstract VariantResult toVariantResponse(GetProductResult.Variant variant);
}

