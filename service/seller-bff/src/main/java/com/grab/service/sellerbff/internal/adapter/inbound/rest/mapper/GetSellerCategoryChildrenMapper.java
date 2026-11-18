package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.service.sellerbff.internal.query.model.SellerCategoryQueries.GetSellerCategoryChildrenQuery;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryChildrenMapper {

    public abstract GetSellerCategoryChildrenQuery toQuery(String categoryId);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);

    public abstract List<CategoryResponse> toResponseList(List<CategoryOutboundResult> results);
}

