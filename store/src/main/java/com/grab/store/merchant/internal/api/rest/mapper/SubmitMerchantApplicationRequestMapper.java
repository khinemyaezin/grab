package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SubmitMerchantApplicationRequestMapper {
    public abstract SubmitMerchantApplicationCommand toCommand(String merchantId, String applicantUserId);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
