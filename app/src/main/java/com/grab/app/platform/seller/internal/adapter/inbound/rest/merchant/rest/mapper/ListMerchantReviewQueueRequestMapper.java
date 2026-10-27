package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
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
