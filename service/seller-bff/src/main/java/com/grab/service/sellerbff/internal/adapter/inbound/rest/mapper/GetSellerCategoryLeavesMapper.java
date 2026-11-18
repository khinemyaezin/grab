package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.service.sellerbff.internal.query.model.SellerCategoryQueries.GetSellerCategoryLeavesQuery;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryLeavesMapper {

    public abstract GetSellerCategoryLeavesQuery toQuery(String name);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);

    public abstract List<CategoryResponse> toResponseList(List<CategoryOutboundResult> results);
}

