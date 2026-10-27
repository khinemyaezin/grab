package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.GetVariantResponse;
import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetVariantDtoMapper {

    public abstract GetVariantQuery toQuery(String merchantId, String productId, String variantId);

    public abstract GetVariantResponse toResponse(GetVariantResult result);
}
