package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.RegisterUseCase;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements CommandHandler<RegisterCommand, UserProfileResult> {

    private final RegisterUseCase registerUseCase;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(RegisterCommand command) {
        return registerUseCase.execute(command);
    }

    @Override
    public Class<RegisterCommand> getCommandType() {
        return RegisterCommand.class;
    }
}
