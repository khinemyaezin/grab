package com.grab.store.merchant.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.merchant.internal.api.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.StartMerchantApplicationCommand;
import com.merchant.domain.enums.MerchantType;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartMerchantApplicationRequestMapper {
    public abstract StartMerchantApplicationCommand toCommand(
            String applicantUserId, MerchantType type, StartMerchantApplicationRequest request);

    public abstract MerchantResponse toResponse(MerchantAccountResult result);
}
