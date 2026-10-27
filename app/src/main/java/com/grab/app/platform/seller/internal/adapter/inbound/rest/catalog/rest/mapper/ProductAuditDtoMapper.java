package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.ProductAuditResponse;
import com.catalog.application.model.read.GetProductAuditResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ProductAuditDtoMapper {
    public abstract ProductAuditResponse toResponse(GetProductAuditResult result);
}
