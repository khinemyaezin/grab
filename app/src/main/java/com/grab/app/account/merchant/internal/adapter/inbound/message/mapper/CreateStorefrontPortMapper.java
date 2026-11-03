package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.CreateStorefrontMessage;
import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.StorefrontMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateStorefrontPortMapper {

    public abstract CreateStorefrontCommand toCommand(CreateStorefrontMessage message);

    public abstract StorefrontMessageResult toResponse(StorefrontResult result);
}

