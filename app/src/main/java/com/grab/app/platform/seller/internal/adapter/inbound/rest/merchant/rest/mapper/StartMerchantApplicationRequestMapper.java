package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.domain.enums.MerchantType;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartMerchantApplicationRequestMapper {
    public abstract StartMerchantApplicationCommand toCommand(
            String applicantUserId, MerchantType type, StartMerchantApplicationRequest request);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
