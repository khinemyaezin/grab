package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.SubmitMerchantApplicationCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SubmitMerchantApplicationRequestMapper {
    public abstract SubmitMerchantApplicationCommand toCommand(String merchantId, String applicantUserId);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
