package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.SwitchAccessContextCommand;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerAccessContextCommandService {

    private final CommandBus commandBus;

    public SellerAccessContextCommandService(@Qualifier("sellerCommandBus") CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    public SellerAuthResponse switchContext(String assignmentId, String currentRefreshToken, SellerSecurityPrincipal principal) {
        String userId = principal != null ? principal.getPlatformUserId() : null;
        return commandBus.dispatch(new SwitchAccessContextCommand(userId, assignmentId, currentRefreshToken));
    }
}

