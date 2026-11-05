package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateProductStatusPortMapper {

    public UpdateProductStatusCommand toCommand(Id merchantId, Id productId, String status) {
        return new UpdateProductStatusCommand(merchantId, productId, status);
    }

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "status", source = "newStatus")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "condition", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "variants", expression = "java(java.util.List.of())")
    public abstract ProductResult toResponse(UpdateProductStatusResult result);
}

