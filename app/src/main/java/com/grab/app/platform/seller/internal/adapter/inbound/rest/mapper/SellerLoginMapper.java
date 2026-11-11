package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerLoginCommand;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLoginMapper {

    public abstract SellerLoginCommand toCommand(SellerLoginRequest request);

    public abstract SellerAuthResponse toResponse(SellerAuthTokensResult result);
}

