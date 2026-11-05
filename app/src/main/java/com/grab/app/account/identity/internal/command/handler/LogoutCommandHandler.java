package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.LogoutCommand;
import com.identity.application.port.inbound.LogoutUseCase;
import org.springframework.stereotype.Component;

@Component
public class LogoutCommandHandler implements CommandHandler<LogoutCommand, Void> {

    private final LogoutUseCase logoutUseCase;

    public LogoutCommandHandler(LogoutUseCase logoutUseCase) {
        this.logoutUseCase = logoutUseCase;
    }

    @Override
    @IdentityTransactional
    public Void handle(LogoutCommand command) {
        logoutUseCase.execute(command);
        return null;
    }

    @Override
    public Class<LogoutCommand> getCommandType() {
        return LogoutCommand.class;
    }
}

