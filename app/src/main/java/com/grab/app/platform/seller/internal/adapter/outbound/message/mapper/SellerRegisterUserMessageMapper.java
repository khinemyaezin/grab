package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRegisterUserMessageMapper {

    public AuthenticationPort.RegisterUserMessage toMessage(String email, String password) {
        return new AuthenticationPort.RegisterUserMessage(email, password);
    }

    public abstract SellerUserRegistrationResult toResult(AuthenticationPort.UserRegistrationResult result);
}

