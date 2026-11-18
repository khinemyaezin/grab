package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegisterUserRequest;
import com.grab.service.contract.message.account.identity.AuthenticationPort.RegisterUserMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.UserRegistrationResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RegisterUserPortMapper {

    public abstract RegisterCommand toCommand(RegisterUserRequest message);

    public abstract RegisterCommand toCommand(RegisterUserMessage message);

    public abstract UserRegistrationResult toResponse(UserProfileResult result);
}

