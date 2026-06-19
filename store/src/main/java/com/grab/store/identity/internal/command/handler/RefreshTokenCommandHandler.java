package com.grab.store.identity.internal.command.handler;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.*;
import com.identity.domain.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, AuthResult> {
    private final TokenIssuer issuer;
    @Override public AuthResult handle(RefreshTokenCommand c) { TokenPair p = issuer.refresh(c.refreshToken()); return new AuthResult(p.accessToken(), p.refreshToken(), p.expiresInMs(), null, null, java.util.Set.of(), "ACTIVE"); }
    @Override public Class<RefreshTokenCommand> getCommandType() { return RefreshTokenCommand.class; }
}
