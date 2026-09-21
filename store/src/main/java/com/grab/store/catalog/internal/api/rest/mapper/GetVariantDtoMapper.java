package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.response.GetVariantResponse;
import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetVariantDtoMapper {

    public abstract GetVariantQuery toQuery(String merchantId, String productId, String variantId);

    public abstract GetVariantResponse toResponse(GetVariantResult result);
}
