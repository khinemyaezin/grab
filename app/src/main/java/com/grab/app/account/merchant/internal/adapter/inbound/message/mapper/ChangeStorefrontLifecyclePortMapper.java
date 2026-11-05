package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.StorefrontMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.StorefrontResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeStorefrontLifecyclePortMapper {

    public ChangeStorefrontLifecycleCommand toCommand(Id storefrontId, Id merchantId, ChangeStorefrontLifecycleCommand.Action action, String reason) {
        return new ChangeStorefrontLifecycleCommand(storefrontId, merchantId, action, reason);
    }

    public abstract StorefrontMessageResult toResponse(StorefrontResult result);
}
