package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.ChangeUserStatusUseCase;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeUserStatusCommandHandler implements CommandHandler<ChangeUserStatusCommand, UserProfileResult> {

    private final ChangeUserStatusUseCase changeUserStatusUseCase;

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
