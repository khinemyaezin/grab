package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.*;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.*;
import com.identity.application.model.write.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final CommandBus commandBus;
    private final RegisterRequestMapper registerMapper;
    private final LoginRequestMapper loginMapper;
    private final RefreshTokenRequestMapper refreshMapper;
    private final LogoutRequestMapper logoutMapper;

    public UserProfileResponse register(RegisterRequest r) {
        RegisterCommand command = registerMapper.toCommand(r);
        UserProfileResult result = commandBus.dispatch(command);
        return registerMapper.toResponse(result);
    }

    public AuthResponse login(LoginRequest r) {
        LoginCommand command = loginMapper.toCommand(r);
        AuthResult result =  commandBus.dispatch(command);
        return loginMapper.toResponse(result);
    }

    public AuthResponse refresh(RefreshTokenRequest r) {
        RefreshTokenCommand command = refreshMapper.toCommand(r);
        AuthResult result = commandBus.dispatch(command);
        return refreshMapper.toResponse(result);
    }

    public void logout(LogoutRequest r) {
        commandBus.dispatch(logoutMapper.toCommand(r));
    }
}
