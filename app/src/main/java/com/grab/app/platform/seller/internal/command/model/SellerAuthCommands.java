package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.app.platform.seller.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.framework.cqrs.command.Command;

public final class SellerAuthCommands {

    private SellerAuthCommands() {}

    public record SellerLoginCommand(
            String email,
            String password
    ) implements Command<SellerAuthTokensResult> {}

    public record SellerRefreshTokenCommand(
            String refreshToken
    ) implements Command<SellerAuthTokensResult> {}

    public record SellerLogoutCommand(
            String refreshToken
    ) implements Command<Void> {}

    public record SellerRegisterUserCommand(
            String email,
            String password
    ) implements Command<SellerUserRegistrationResult> {}
}

