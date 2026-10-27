package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.read.GetMerchantQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetMerchantRequestMapper {
    public abstract GetMerchantQuery toQuery(
            String merchantId,
            String actorId,
            boolean reviewerAccess,
            boolean scopedAccess
    );

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
