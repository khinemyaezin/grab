package com.grab.service.sellerbff.internal.adapter.inbound.rest.service;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper.SellerRegisterMapper;
import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerRegisterUserCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerAuthCommandService {

    private final CommandBus commandBus;
    private final SellerRegisterMapper registerMapper;

    public SellerAuthCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            SellerRegisterMapper registerMapper) {
        this.commandBus = commandBus;
        this.registerMapper = registerMapper;
    }

    public SellerUserProfileResponse register(SellerRegisterRequest request) {
        SellerRegisterUserCommand command = registerMapper.toCommand(request);
        SellerUserRegistrationResult result = commandBus.dispatch(command);
        return registerMapper.toResponse(result);
    }
}

