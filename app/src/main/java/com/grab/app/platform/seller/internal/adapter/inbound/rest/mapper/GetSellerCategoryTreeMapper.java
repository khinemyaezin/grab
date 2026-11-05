package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryTreeQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryTreeMapper {

    public abstract GetSellerCategoryTreeQuery toQuery(String rootId);

    @Mapping(target = "parentId", source = "slug")
    public abstract CategoryNodeResponse toResponse(CategoryNodeOutboundResult result);
}

