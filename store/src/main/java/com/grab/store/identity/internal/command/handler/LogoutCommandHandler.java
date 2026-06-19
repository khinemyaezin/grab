package com.grab.store.identity.internal.command.handler;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.LogoutCommand;
import com.identity.domain.service.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class LogoutCommandHandler implements CommandHandler<LogoutCommand, Void> {
    private final TokenIssuer issuer;
    @Override public Void handle(LogoutCommand c) { issuer.revoke(c.refreshToken()); return null; }
    @Override public Class<LogoutCommand> getCommandType() { return LogoutCommand.class; }
}
