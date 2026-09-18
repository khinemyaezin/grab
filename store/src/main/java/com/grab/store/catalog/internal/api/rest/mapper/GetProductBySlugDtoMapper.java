package com.grab.store.catalog.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetProductBySlugDtoMapper {
    @Mapping(source = "merchantId", target = "sellerId")
    @Mapping(source = "merchantType", target = "sellerType")
    @Mapping(target = "offerEligible", constant = "false")
    @Mapping(target = "moderationNote", ignore = true)
    public abstract GetProductBySlugResponse toResponse(GetProductBySlugResult result);
}
