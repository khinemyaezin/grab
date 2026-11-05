package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.LoginPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.LogoutPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.RefreshTokenPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.RegisterUserPortMapper;
import com.grab.app.account.identity.port.message.AuthenticationPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.LoginCommand;
import com.identity.application.model.write.LogoutCommand;
import com.identity.application.model.write.RefreshTokenCommand;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationPortAdapter implements AuthenticationPort {

    private final CommandBus commandBus;
    private final LoginPortMapper loginMapper;
    private final RefreshTokenPortMapper refreshTokenMapper;
    private final LogoutPortMapper logoutMapper;
    private final RegisterUserPortMapper registerUserMapper;

    public AuthenticationPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            LoginPortMapper loginMapper,
            RefreshTokenPortMapper refreshTokenMapper,
            LogoutPortMapper logoutMapper,
            RegisterUserPortMapper registerUserMapper) {
        this.commandBus = commandBus;
        this.loginMapper = loginMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.logoutMapper = logoutMapper;
        this.registerUserMapper = registerUserMapper;
    }

    @Override
    public AuthTokensResult login(LoginMessage message) {
        LoginCommand command = loginMapper.toCommand(message);
        AuthResult tokens = commandBus.dispatch(command);
        return loginMapper.toResponse(tokens);
    }

    @Override
    public AuthTokensResult refreshToken(RefreshTokenMessage message) {
        RefreshTokenCommand command = refreshTokenMapper.toCommand(message);
        AuthResult tokens = commandBus.dispatch(command);
        return refreshTokenMapper.toResponse(tokens);
    }

    @Override
    public void logout(LogoutMessage message) {
        LogoutCommand command = logoutMapper.toCommand(message);
        commandBus.dispatch(command);
    }

    @Override
    public UserRegistrationResult registerUser(RegisterUserMessage message) {
        RegisterCommand command = registerUserMapper.toCommand(message);
        UserProfileResult profile = commandBus.dispatch(command);
        return registerUserMapper.toResponse(profile);
    }
}
