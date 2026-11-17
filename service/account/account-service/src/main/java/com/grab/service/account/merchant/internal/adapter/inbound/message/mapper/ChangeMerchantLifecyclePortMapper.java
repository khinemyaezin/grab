package com.grab.service.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantAccountPort.MerchantMessageResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeMerchantLifecyclePortMapper {

    public ChangeMerchantLifecycleCommand toCommand(Id merchantId, Id actorId, ChangeMerchantLifecycleCommand.Action action, String reason) {
        return new ChangeMerchantLifecycleCommand(merchantId, actorId, action, reason);
    }

    @Mapping(target = "businessType", source = "type")
    public abstract MerchantMessageResult toResponse(MerchantAccountResult result);
}

