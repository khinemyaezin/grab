package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StorefrontQueryRequestMapper {
    public abstract GetStorefrontQuery toGetQuery(String storefrontId, String merchantId);

    public abstract ListStorefrontsByMerchantQuery toListQuery(String merchantId);

    public abstract StorefrontResponse toResponse(StorefrontResult result);

    public abstract List<StorefrontResponse> toResponse(List<StorefrontResult> results);
}
