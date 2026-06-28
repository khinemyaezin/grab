package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.query.ListMerchantReviewQueueQuery;
import com.merchant.domain.enums.MerchantStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantReviewQueueRequestMapper {
    public abstract ListMerchantReviewQueueQuery toQuery(MerchantStatus status);

    public abstract List<MerchantResponse> toResponse(List<MerchantAccountResult> results);
}
