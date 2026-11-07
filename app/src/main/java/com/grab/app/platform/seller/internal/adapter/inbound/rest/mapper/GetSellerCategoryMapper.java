package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerCategoryMapper {

    public abstract GetSellerCategoryQuery toQuery(String categoryId);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);
}

