package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.identity.AuthenticationPort;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLogoutMessageMapper {

    public AuthenticationPort.LogoutMessage toMessage(String refreshToken) {
        return new AuthenticationPort.LogoutMessage(refreshToken);
    }
}

