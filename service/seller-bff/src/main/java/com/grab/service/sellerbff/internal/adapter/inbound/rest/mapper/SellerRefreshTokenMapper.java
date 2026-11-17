package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerRefreshTokenRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerRefreshTokenCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRefreshTokenMapper {

    public abstract SellerRefreshTokenCommand toCommand(SellerRefreshTokenRequest request);

    public abstract SellerAuthResponse toResponse(SellerAuthTokensResult result);
}

