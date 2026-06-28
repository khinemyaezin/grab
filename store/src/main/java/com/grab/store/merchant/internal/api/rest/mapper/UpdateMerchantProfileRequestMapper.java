package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.UpdateMerchantProfileCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateMerchantProfileRequestMapper {
    public abstract UpdateMerchantProfileCommand toCommand(
            String merchantId, String applicantUserId, UpdateMerchantProfileRequest request);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
