package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.account.identity.port.message.AuthenticationPort;
import com.grab.app.account.identity.port.message.AuthenticationPort.AuthTokensResult;
import com.grab.app.account.identity.port.message.AuthenticationPort.LoginMessage;
import com.grab.app.account.identity.port.message.AuthenticationPort.LogoutMessage;
import com.grab.app.account.identity.port.message.AuthenticationPort.RefreshTokenMessage;
import com.grab.app.account.identity.port.message.AuthenticationPort.RegisterUserMessage;
import com.grab.app.account.identity.port.message.AuthenticationPort.UserRegistrationResult;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SellerLoginMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SellerLogoutMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SellerRefreshTokenMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SellerRegisterUserMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthMessageAdapter implements SellerAuthOutboundPort {

    private final AuthenticationPort authenticationPort;
    private final SellerLoginMessageMapper loginMessageMapper;
    private final SellerRefreshTokenMessageMapper refreshTokenMessageMapper;
    private final SellerLogoutMessageMapper logoutMessageMapper;
    private final SellerRegisterUserMessageMapper registerUserMessageMapper;

    public SellerAuthMessageAdapter(
            AuthenticationPort authenticationPort,
            SellerLoginMessageMapper loginMessageMapper,
            SellerRefreshTokenMessageMapper refreshTokenMessageMapper,
            SellerLogoutMessageMapper logoutMessageMapper,
            SellerRegisterUserMessageMapper registerUserMessageMapper) {
        this.authenticationPort = authenticationPort;
        this.loginMessageMapper = loginMessageMapper;
        this.refreshTokenMessageMapper = refreshTokenMessageMapper;
        this.logoutMessageMapper = logoutMessageMapper;
        this.registerUserMessageMapper = registerUserMessageMapper;
    }

    @Override
    public SellerAuthTokensResult login(String email, String password) {
        LoginMessage message = loginMessageMapper.toMessage(email, password);
        AuthTokensResult result = authenticationPort.login(message);
        return loginMessageMapper.toResult(result);
    }

    @Override
    public SellerAuthTokensResult refreshToken(String refreshToken) {
        RefreshTokenMessage message = refreshTokenMessageMapper.toMessage(refreshToken);
        AuthTokensResult result = authenticationPort.refreshToken(message);
        return refreshTokenMessageMapper.toResult(result);
    }

    @Override
    public void logout(String refreshToken) {
        LogoutMessage message = logoutMessageMapper.toMessage(refreshToken);
        authenticationPort.logout(message);
    }

    @Override
    public SellerUserRegistrationResult registerUser(String email, String password) {
        RegisterUserMessage message = registerUserMessageMapper.toMessage(email, password);
        UserRegistrationResult result = authenticationPort.registerUser(message);
        return registerUserMessageMapper.toResult(result);
    }
}

