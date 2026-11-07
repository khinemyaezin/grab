package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryLeavesQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryLeavesMapper {

    public abstract GetSellerCategoryLeavesQuery toQuery(String name);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);

    public abstract List<CategoryResponse> toResponseList(List<CategoryOutboundResult> results);
}

