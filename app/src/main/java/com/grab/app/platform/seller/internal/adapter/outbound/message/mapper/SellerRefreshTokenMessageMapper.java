package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SellerRefreshTokenMessageMapper {

    public AuthenticationPort.RefreshTokenMessage toMessage(String refreshToken) {
        return new AuthenticationPort.RefreshTokenMessage(refreshToken);
    }

    public abstract SellerAuthTokensResult toResult(AuthenticationPort.AuthTokensResult result);
}

