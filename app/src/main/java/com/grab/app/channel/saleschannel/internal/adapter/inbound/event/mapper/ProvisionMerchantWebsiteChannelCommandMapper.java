package com.grab.app.channel.saleschannel.internal.adapter.inbound.event.mapper;

import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProvisionMerchantWebsiteChannelCommandMapper {
    @Mapping(source = "merchantId", target = "merchantId")
    @Mapping(source = "primaryMarketCountryCode", target = "primaryMarketCountryCode")
    public abstract ProvisionMerchantWebsiteChannelCommand toCommand(MerchantApprovedIntegrationEvent event);
}
