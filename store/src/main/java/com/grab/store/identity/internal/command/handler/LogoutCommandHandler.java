package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.LogoutUseCase;
import com.identity.application.model.write.LogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutCommandHandler implements CommandHandler<LogoutCommand, Void> {

    private final LogoutUseCase logoutUseCase;

    @Override
    @IdentityTransactional
    public Void handle(LogoutCommand command) {
        return logoutUseCase.execute(command);
    }

    @Override
    public Class<LogoutCommand> getCommandType() {
        return LogoutCommand.class;
    }
}
