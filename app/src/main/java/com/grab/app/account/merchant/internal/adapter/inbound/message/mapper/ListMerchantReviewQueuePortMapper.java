package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort.MerchantMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.domain.enums.MerchantStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantReviewQueuePortMapper {

    public ListMerchantReviewQueueQuery toQuery(String status) {
        return new ListMerchantReviewQueueQuery(status != null ? MerchantStatus.valueOf(status) : null);
    }

    @Mapping(target = "businessType", source = "type")
    public abstract MerchantMessageResult toResponse(MerchantAccountResult result);
}
