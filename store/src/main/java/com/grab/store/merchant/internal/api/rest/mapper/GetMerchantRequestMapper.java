package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.query.GetMerchantQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetMerchantRequestMapper {
    public abstract GetMerchantQuery toQuery(String merchantId, String actorId, boolean reviewerAccess);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
