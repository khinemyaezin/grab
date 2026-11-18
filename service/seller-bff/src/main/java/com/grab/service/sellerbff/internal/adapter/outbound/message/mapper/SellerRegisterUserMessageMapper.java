package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRegisterUserMessageMapper {

    public SellerRegistrationPort.RegisterUserRequest toMessage(String email, String password) {
        return new SellerRegistrationPort.RegisterUserRequest(email, password);
    }

    public SellerUserRegistrationResult toResult(SellerRegistrationPort.RegistrationResult result) {
        return new SellerUserRegistrationResult(result.id(), result.email(), result.userStatus(), result.createdAt());
    }
}

