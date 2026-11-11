package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLogoutRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRefreshTokenRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SellerLoginMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SellerLogoutMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SellerRefreshTokenMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SellerRegisterMapper;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerLoginCommand;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerLogoutCommand;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerRefreshTokenCommand;
import com.grab.app.platform.seller.internal.command.model.SellerAuthCommands.SellerRegisterUserCommand;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerAuthCommandService {

    private final CommandBus commandBus;
    private final SellerLoginMapper loginMapper;
    private final SellerRefreshTokenMapper refreshTokenMapper;
    private final SellerLogoutMapper logoutMapper;
    private final SellerRegisterMapper registerMapper;

    public SellerAuthCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            SellerLoginMapper loginMapper,
            SellerRefreshTokenMapper refreshTokenMapper,
            SellerLogoutMapper logoutMapper,
            SellerRegisterMapper registerMapper) {
        this.commandBus = commandBus;
        this.loginMapper = loginMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.logoutMapper = logoutMapper;
        this.registerMapper = registerMapper;
    }

    public SellerAuthResponse login(SellerLoginRequest request) {
        SellerLoginCommand command = loginMapper.toCommand(request);
        SellerAuthTokensResult result = commandBus.dispatch(command);
        return loginMapper.toResponse(result);
    }

    public SellerAuthResponse refresh(SellerRefreshTokenRequest request) {
        SellerRefreshTokenCommand command = refreshTokenMapper.toCommand(request);
        SellerAuthTokensResult result = commandBus.dispatch(command);
        return refreshTokenMapper.toResponse(result);
    }

    public void logout(SellerLogoutRequest request) {
        SellerLogoutCommand command = logoutMapper.toCommand(request);
        commandBus.dispatch(command);
    }

    public SellerUserProfileResponse register(SellerRegisterRequest request) {
        SellerRegisterUserCommand command = registerMapper.toCommand(request);
        SellerUserRegistrationResult result = commandBus.dispatch(command);
        return registerMapper.toResponse(result);
    }
}

