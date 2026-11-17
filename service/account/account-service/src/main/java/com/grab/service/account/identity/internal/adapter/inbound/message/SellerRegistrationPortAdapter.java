package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.RegisterUserPortMapper;
import com.grab.service.contract.message.account.identity.AuthenticationPort.RegisterUserMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.UserRegistrationResult;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SellerRegistrationPortAdapter implements SellerRegistrationPort {
    private final CommandBus commands;
    private final RegisterUserPortMapper mapper;

    public SellerRegistrationPortAdapter(@Qualifier("accountCommandBus") CommandBus commands, RegisterUserPortMapper mapper) {
        this.commands = commands;
        this.mapper = mapper;
    }

    @Override
    public UserRegistrationResult registerUser(RegisterUserMessage request) {
        RegisterCommand command = mapper.toCommand(request);
        UserProfileResult result = commands.dispatch(command);
        return mapper.toResponse(result);
    }
}
