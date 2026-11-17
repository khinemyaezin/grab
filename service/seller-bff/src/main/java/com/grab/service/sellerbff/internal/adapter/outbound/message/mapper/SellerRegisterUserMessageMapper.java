package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.identity.AuthenticationPort;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRegisterUserMessageMapper {

    public AuthenticationPort.RegisterUserMessage toMessage(String email, String password) {
        return new AuthenticationPort.RegisterUserMessage(email, password);
    }

    public abstract SellerUserRegistrationResult toResult(AuthenticationPort.UserRegistrationResult result);
}

