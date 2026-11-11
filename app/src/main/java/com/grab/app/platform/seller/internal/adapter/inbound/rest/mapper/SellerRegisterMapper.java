package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerRegisterUserCommand;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRegisterMapper {

    public abstract SellerRegisterUserCommand toCommand(SellerRegisterRequest request);

    public abstract SellerUserProfileResponse toResponse(SellerUserRegistrationResult result);
}

