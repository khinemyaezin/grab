package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMyMerchantsRequestMapper {
    public abstract ListMyMerchantsQuery toQuery(String applicantUserId);

    public abstract List<MerchantResponse> toResponse(List<MerchantAccountResult> results);
}
