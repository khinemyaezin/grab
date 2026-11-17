package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerLoginRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerLoginCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLoginMapper {

    public abstract SellerLoginCommand toCommand(SellerLoginRequest request);

    public abstract SellerAuthResponse toResponse(SellerAuthTokensResult result);
}

