package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.ProvisionWebsiteChannelRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.service.sellerbff.internal.command.model.SellerSalesChannelCommands.ProvisionSellerMerchantWebsiteChannelCommand;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ProvisionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProvisionSellerSalesChannelMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "countryCode", source = "request.countryCode")
    public abstract ProvisionSellerMerchantWebsiteChannelCommand toCommand(String merchantId, ProvisionWebsiteChannelRequest request);

    public abstract SalesChannelProvisionResponse toResponse(ProvisionOutboundResult result);
}
