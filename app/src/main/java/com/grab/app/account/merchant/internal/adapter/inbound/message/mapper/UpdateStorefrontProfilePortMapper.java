package com.grab.app.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.StorefrontMessageResult;
import com.grab.app.account.merchant.port.message.MerchantStorefrontPort.UpdateStorefrontMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateStorefrontProfilePortMapper {

    public UpdateStorefrontProfileCommand toCommand(Id storefrontId, Id merchantId, UpdateStorefrontMessage message) {
        if (message == null) {
            return null;
        }
        return new UpdateStorefrontProfileCommand(storefrontId, merchantId, message.name(), message.slug());
    }

    public abstract StorefrontMessageResult toResponse(StorefrontResult result);
}

