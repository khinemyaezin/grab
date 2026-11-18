package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerRegisterUserCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRegisterMapper {

    public abstract SellerRegisterUserCommand toCommand(SellerRegisterRequest request);

    public abstract SellerUserProfileResponse toResponse(SellerUserRegistrationResult result);
}

