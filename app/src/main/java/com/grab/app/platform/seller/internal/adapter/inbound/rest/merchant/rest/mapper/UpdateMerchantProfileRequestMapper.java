package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateMerchantProfileRequestMapper {
    public abstract UpdateMerchantProfileCommand toCommand(
            String merchantId, String applicantUserId, UpdateMerchantProfileRequest request);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
