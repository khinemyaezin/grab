package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.store.catalog.internal.api.rest.dto.response.ProductAuditResponse;
import com.grab.store.catalog.internal.query.GetProductAuditResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ProductAuditDtoMapper {
    public abstract ProductAuditResponse toResponse(GetProductAuditResult result);
}
