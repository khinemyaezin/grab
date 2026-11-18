package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.service.sellerbff.internal.query.model.SellerCategoryQueries.GetSellerCategoryQuery;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryMapper {

    public abstract GetSellerCategoryQuery toQuery(String categoryId);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);
}

