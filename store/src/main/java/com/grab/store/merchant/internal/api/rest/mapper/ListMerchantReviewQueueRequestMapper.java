package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import com.merchant.domain.enums.MerchantStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantReviewQueueRequestMapper {
    public abstract ListMerchantReviewQueueQuery toQuery(MerchantStatus status);

    public abstract List<MerchantResponse> toResponse(List<MerchantAccountResult> results);
}
