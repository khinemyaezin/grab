package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerLogoutMessageMapper {

    public AuthenticationPort.LogoutMessage toMessage(String refreshToken) {
        return new AuthenticationPort.LogoutMessage(refreshToken);
    }
}

