package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.SalesChannelManagementPort.ProvisionResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProvisionSalesChannelPortMapper {

    public ProvisionMerchantWebsiteChannelCommand toCommand(Id merchantId, String countryCode) {
        return new ProvisionMerchantWebsiteChannelCommand(merchantId, countryCode);
    }

    public abstract ProvisionResult toResponse(ProvisionSalesChannelResult result);
}

