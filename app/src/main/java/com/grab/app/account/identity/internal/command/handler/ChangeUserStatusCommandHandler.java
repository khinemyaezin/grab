package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.port.inbound.ChangeUserStatusUseCase;
import org.springframework.stereotype.Component;

@Component
public class ChangeUserStatusCommandHandler implements CommandHandler<ChangeUserStatusCommand, UserProfileResult> {

    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    public ChangeUserStatusCommandHandler(ChangeUserStatusUseCase changeUserStatusUseCase) {
        this.changeUserStatusUseCase = changeUserStatusUseCase;
    }

    @Override
    @IdentityTransactional
    public UserProfileResult handle(ChangeUserStatusCommand command) {
        return changeUserStatusUseCase.execute(command);
    }

    @Override
    public Class<ChangeUserStatusCommand> getCommandType() {
        return ChangeUserStatusCommand.class;
    }
}
